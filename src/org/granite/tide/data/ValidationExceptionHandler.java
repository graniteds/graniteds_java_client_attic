package org.granite.tide.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.granite.tide.Context;
import org.granite.tide.server.ExceptionHandler;
import org.granite.tide.server.TideFaultEvent;
import org.granite.validation.InvalidValue;
import org.granite.validation.ServerConstraintViolation;

import flex.messaging.messages.ErrorMessage;


public class ValidationExceptionHandler implements ExceptionHandler {

	@Override
	public boolean accepts(ErrorMessage emsg) {
		return emsg.getFaultCode().equals("Validation.Failed");
	}

	@Override
	public void handle(Context context, ErrorMessage emsg, TideFaultEvent faultEvent) {
		Object[] invalidValues = (Object[])emsg.getExtendedData().get("invalidValues");
		if (invalidValues != null) {
			Map<Object, Set<ConstraintViolation<?>>> violationsMap = new HashMap<Object, Set<ConstraintViolation<?>>>();
			for (Object v : invalidValues) {
				InvalidValue iv = (InvalidValue)v;
				Object rootBean = context.getEntityManager().getCachedObject(iv.getRootBean(), true);
				Object leafBean = iv.getBean() != null ? context.getEntityManager().getCachedObject(iv.getBean(), true) : null;
				Object bean = leafBean != null ? leafBean : rootBean;
				
				Set<ConstraintViolation<?>> violations = violationsMap.get(bean);
				if (violations == null) {
					violations = new HashSet<ConstraintViolation<?>>();
					violationsMap.put(bean, violations);
				}
				
				ServerConstraintViolation violation = new ServerConstraintViolation(iv, rootBean, leafBean);
				violations.add(violation);
			}
			
			for (Object bean : violationsMap.keySet())
				context.getDataManager().notifyConstraintViolations(bean, violationsMap.get(bean));
		}
	}

}
