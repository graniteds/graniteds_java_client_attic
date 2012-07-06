package org.granite.client.validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;



/**
 * Represents a constraint violation received from the server.
 * 
 * @author William DRAI
 */
public class ServerConstraintViolation implements ConstraintViolation<Object> {
	
	private InvalidValue invalidValue;
	private Object rootBean;
	private Object bean;
	private Path propertyPath;
	private String message;
	
	
	/**
	 * Constructs a new <code>ServerConstraintViolation</code> instance.
	 * 
	 * @param invalidValue serialized server-side ConstraintViolation
	 * @param rootBean root bean
	 * @param bean leaf bean
	 */
	public ServerConstraintViolation(InvalidValue invalidValue, Object rootBean, Object bean) {
		this.rootBean = rootBean;
		this.bean = bean;		
		this.propertyPath = new PathImpl(invalidValue.getPath());
		this.message = invalidValue.getMessage();
	}


	public InvalidValue getInvalidValue() {
		return invalidValue;
	}

	public Object getRootBean() {
		return rootBean;
	}

	@Override
	public Class<Object> getRootBeanClass() {
		return Object.class;
	}

	public Object getLeafBean() {
		return bean;
	}

	public Path getPropertyPath() {
		return propertyPath;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getMessageTemplate() {
		return message;
	}

	@Override
	public ConstraintDescriptor<?> getConstraintDescriptor() {
		return null;
	}
	
	public class PathImpl implements Path {
		
		private List<Node> nodeList = new ArrayList<Node>();
		
		public PathImpl(final String path) {
			nodeList.add(new Node() {
				@Override
				public boolean isInIterable() {
					return true;
				}
				
				@Override
				public String getName() {
					return path;
				}
				
				@Override
				public Object getKey() {
					return null;
				}
				
				@Override
				public Integer getIndex() {
					return null;
				}
			});
		}
		
		@Override
		public Iterator<Node> iterator() {
			return nodeList.iterator();
		}
		
	}
}
