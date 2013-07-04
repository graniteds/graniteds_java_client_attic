package org.granite.client.test.jmf;

import java.io.Serializable;
import java.util.List;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Id;
import org.granite.client.persistence.Uid;
import org.granite.client.persistence.Version;

@Entity
@RemoteAlias("org.granite.client.test.jmf.ServerEntity")
public class ClientEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private boolean __initialized__ = true;
	@SuppressWarnings("unused")
	private String __detachedState__ = null;
	
	@Id
	private Integer id;
	
	@Uid
	private String uid;
	
	@Version
	private Integer version;

	private String name;
	
	private List<ClientCollectionEntity> list;

	public Integer getId() {
		return id;
	}

	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}

	public Integer getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ClientCollectionEntity> getList() {
		return list;
	}

	public void setList(List<ClientCollectionEntity> list) {
		this.list = list;
	}
}
