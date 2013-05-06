package org.granite.client.test.model;

import java.io.Serializable;

import org.granite.client.persistence.Version;

public abstract class Versioned implements Serializable {

	private static final long serialVersionUID = 1L;

	@Version
    private Integer version;

    public Integer getVersion() {
        return version;
    }
}
