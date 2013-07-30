package org.granite.client.test.model.embed;

import java.io.Serializable;

import org.granite.client.messaging.RemoteAlias;

@RemoteAlias("org.granite.example.addressbook.entity.embed.Document")
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String contentType;
    private byte[] content;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }
    public void setContent(byte[] content) {
        this.content = content;
    }
}
