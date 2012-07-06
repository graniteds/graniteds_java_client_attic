package org.granite.client.messaging.channel;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.granite.util.Base64;

public final class UsernamePasswordCredentials implements Credentials {

	private final String username;
	private final String password;
	private final Charset charset;

	public UsernamePasswordCredentials(String username, String password) {
		this(username, password, null);
	}

	public UsernamePasswordCredentials(String username, String password, Charset charset) {
		this.username = username;
		this.password = password;
		this.charset = (charset != null ? charset : Charset.defaultCharset());
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Charset getCharset() {
		return charset;
	}
	
	public String encodeBase64() throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		if (username != null) {
			if (username.indexOf(':') != -1)
				throw new UnsupportedEncodingException("Username cannot contain ':' characters: " + username);
			sb.append(username);
		}
		sb.append(':');
		if (username != null)
			sb.append(password);
		return Base64.encodeToString(sb.toString().getBytes(charset.name()), false);
	}

	@Override
	public String toString() {
		return getClass().getName() + " {username=***, password=***, charset=" + charset + "}";
	}
}
