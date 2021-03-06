package org.granite.client.test.model;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;

@RemoteAlias("org.granite.example.addressbook.entity.Address")
@Entity
public class Address extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    private String address1;
    private String address2;
    private String zipcode;
    private String city;
    private Country country;

    public String getAddress1() {
        return address1;
    }
    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }
    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getZipcode() {
        return zipcode;
    }
    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public Country getCountry() {
        return country;
    }
    public void setCountry(Country country) {
        this.country = country;
    }
}
