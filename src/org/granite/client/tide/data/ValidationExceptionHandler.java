package org.granite.client.tide.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.FaultMessage.Code;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.ExceptionHandler;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.validation.InvalidValue;
import org.granite.client.validation.ServerConstraintViolation;


public class ValidationExceptionHandler implements ExceptionHandler {

	@Override
	public boolean accepts(FaultMessage emsg) {
		return emsg.getCode().equals(Code.VALIDATION_FAILED);
	}

	@Override
	public void handle(Context context, FaultMessage emsg, TideFaultEvent faultEvent) {
		Object[] invalidValues = (Object[])emsg.getExtended().get("invalidValues");
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
