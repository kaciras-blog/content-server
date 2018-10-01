package net.kaciras.blog.api;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.net.InetAddress;
import java.security.Principal;

@EqualsAndHashCode(of = "id")
@Value
public class WebPrincipal implements Principal {

	public static final int ANYNOMOUS_ID = 0;
	public static final int SYSTEM_ID = 1;
	public static final int ADMIN_ID = 2;

	private final int id;
	private final InetAddress address;

	public boolean isLogined() {
		return id > 1;
	}

	public boolean isSystem() {
		return id == SYSTEM_ID;
	}

	public boolean isAnynomous() {
		return id == ANYNOMOUS_ID;
	}

	public boolean isAdministor() {
		return id == ADMIN_ID;
	}

	@Override
	public String getName() {
		switch (id) {
			case 0:
				return "Anynomous";
			case 1:
				return "System";
		}
		return "Logined " + id;
	}
}
