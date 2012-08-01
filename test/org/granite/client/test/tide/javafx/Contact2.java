package org.granite.client.test.tide.javafx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class Contact2 extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty email = new SimpleStringProperty(this, "email");
    
    
    public Contact2() {
        super();
    }
    
    public Contact2(Long id, Long version, String uid, String email) {
        super(id, version, uid);
        this.email.set(email);
    }    
        
    public StringProperty emailProperty() {
        return email;
    }
    
    public String getEmail() {
        return email.get();
    }
    
    public void setEmail(String email) {
        this.email.set(email);
    }
}
