package org.granite.client.test.model;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;

@RemoteAlias("org.granite.example.addressbook.entity.Country")
@Entity
public class Country extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    private String name;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
