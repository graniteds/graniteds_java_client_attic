package org.granite.client.test.jmf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@Entity
public class ServerEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue
	private Integer id;
	
    @Column(name="ENTITY_UID", unique=true, nullable=false, updatable=false, length=36)
	private String uid;

	@Version
    private Integer version;
	
	@Basic
	private String name;
	
	@OneToMany(mappedBy="collectionEntity")
	private List<ServerCollectionEntity> list = new ArrayList<ServerCollectionEntity>();

	public ServerEntity() {
	}

	public ServerEntity(Integer id, Integer version) {
		this.id = id;
		this.version = version;
	}

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

	public List<ServerCollectionEntity> getList() {
		return list;
	}
	public void setList(List<ServerCollectionEntity> list) {
		this.list = list;
	}
}
