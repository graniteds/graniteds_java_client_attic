package org.granite.client.test.tide.javafx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;


public class PersonEnum extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty firstName = new SimpleStringProperty(this, "firstName");
    private StringProperty lastName = new SimpleStringProperty(this, "lastName");
    private ObjectProperty<Salutation> salutation = new SimpleObjectProperty<Salutation>(this, "salutation");
    private ObservableList<Salutation> salutations = null;
    
    
    public PersonEnum() {
        super();
    }
    
    public PersonEnum(Long id, Long version, String uid, String firstName, String lastName) {
        super(id, version, uid);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
    }
    
    public StringProperty firstNameProperty() {
        return firstName;
    }
    
    public String getFirstName() {
        return firstName.get();
    }
    
    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }
    
    public StringProperty lastNameProperty() {
        return lastName;
    }
    
    public String getLastName() {
        return lastName.get();
    }
    
    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }
    
    public ObjectProperty<Salutation> salutationProperty() {
        return salutation;
    }
    
    public Salutation getSalutation() {
        return salutation.get();
    }
    
    public void setSalutation(Salutation salutation) {
        this.salutation.set(salutation);
    }
    
    public ObservableList<Salutation> getSalutations() {
        return salutations;
    }
    
    public void setSalutations(ObservableList<Salutation> salutations) {
        this.salutations = salutations;
    }
}
