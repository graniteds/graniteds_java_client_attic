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

package org.granite.client.test.tide.javafx;

import org.granite.client.tide.data.Lazy;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;


public class Classification extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty name = new SimpleStringProperty(this, "name");
    private ObservableList<Classification> subclasses = null;
    private ObservableList<Classification> superclasses = null;
    
    
    public Classification() {
        super();
    }
    
    public Classification(Long id, Long version, String uid, String name) {
        super(id, version, uid);
        this.name.set(name);
    }
    
    public Classification(Long id, boolean initialized) {
        super(id, initialized);
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getName() {
        return name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    @Lazy
    public ObservableList<Classification> getSubclasses() {
        return subclasses;
    }
    
    public void setSubclasses(ObservableList<Classification> subclasses) {
        this.subclasses = subclasses;
    }
    
    @Lazy
    public ObservableList<Classification> getSuperclasses() {
        return superclasses;
    }
    
    public void setSuperclasses(ObservableList<Classification> superclasses) {
        this.superclasses = superclasses;
    }
}
