/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.client.tide.javafx;

import java.io.IOException;
import java.io.InputStream;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

import javax.inject.Named;

import org.granite.client.tide.Context;

/**
 * @author William DRAI
 */
public class TideFXMLLoader {

    public static Object load(final Context context, String url, Class<?> controllerClass) throws IOException {
        InputStream fxmlStream = null;
        try {
            fxmlStream = controllerClass.getResourceAsStream(url);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(controllerClass.getResource(url));
            loader.setControllerFactory(new ControllerFactory(context));
        	loader.getNamespace().putAll(context.allByAnnotatedWith(Named.class));
            
            return loader.load(fxmlStream);
        }
        finally {
            if (fxmlStream != null) {
                fxmlStream.close();
            }
        }
    }
	
    public static Object load(String url, final Object controller) throws IOException {
        InputStream fxmlStream = null;
        try {
            fxmlStream = controller.getClass().getResourceAsStream(url);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(controller.getClass().getResource(url));
            loader.setControllerFactory(new Callback<Class<?>, Object>() {
				@Override
				public Object call(Class<?> type) {
					if (type.isInstance(controller))
						return controller;
					try {
						return type.newInstance();
					}
					catch (Exception e) {
						throw new RuntimeException("Could not instantiate controller of class " + type);
					}
				}
            });
            return loader.load(fxmlStream);
        }
        finally {
            if (fxmlStream != null) {
                fxmlStream.close();
            }
        }
    }
    
    public static class ControllerFactory implements Callback<Class<?>, Object> {
    	
    	private final Context context;
    	
    	public ControllerFactory(Context context) {
    		this.context = context;
    	}
    	
    	@Override
    	public Object call(Class<?> type) {
    		return context.byType(type);
    	}
    }
}
