package org.granite.client.test.tide.javafx;

import java.math.BigInteger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;


public class PersonBigNum extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty firstName = new SimpleStringProperty(this, "firstName");
    private StringProperty lastName = new SimpleStringProperty(this, "lastName");
    private ObjectProperty<BigInteger> bigInt = new SimpleObjectProperty<BigInteger>(this, "bigInt");
    private ObservableList<BigInteger> bigInts = null;
    
    
    public PersonBigNum() {
        super();
    }
    
    public PersonBigNum(Long id, Long version, String uid, String firstName, String lastName) {
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
    
    public ObjectProperty<BigInteger> bigIntProperty() {
        return bigInt;
    }
    
    public BigInteger getBigInt() {
        return bigInt.get();
    }
    
    public void setBigInt(BigInteger bigInt) {
        this.bigInt.set(bigInt);
    }
    
    public ObservableList<BigInteger> getBigInts() {
        return bigInts;
    }
    
    public void setBigInts(ObservableList<BigInteger> bigInts) {
        this.bigInts = bigInts;
    }
}
