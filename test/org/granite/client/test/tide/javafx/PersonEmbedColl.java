package org.granite.client.test.tide.javafx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;


public class PersonEmbedColl extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty firstName = new SimpleStringProperty(this, "firstName");
    private StringProperty lastName = new SimpleStringProperty(this, "lastName");
    private ObjectProperty<ContactList> contactList = new SimpleObjectProperty<ContactList>(this, "contactList");
    
    
    public PersonEmbedColl() {
        super();
    }
    
    public PersonEmbedColl(Long id, Long version, String uid, String firstName, String lastName) {
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
    
    public static class ContactList {
        
        private ObservableList<Contact> contacts = null;
        
        public ContactList() {            
        }
        
        public ContactList(ObservableList<Contact> contacts) {
            this.contacts = contacts;
        }
        
        public ObservableList<Contact> getContacts() {
            return contacts;
        }
        
        public void setContacts(ObservableList<Contact> contacts) {
            this.contacts = contacts;
        }
    }
    
    public ObjectProperty<ContactList> contactListProperty() {
        return contactList;
    }
    
    public ContactList getContactList() {
        return contactList.get();
    }
    
    public void setContactList(ContactList contactList) {
        this.contactList.set(contactList);
    }
}
