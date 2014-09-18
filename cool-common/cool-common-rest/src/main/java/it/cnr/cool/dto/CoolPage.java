package it.cnr.cool.dto;

public class CoolPage {

	private final String url;

	public enum Authentication {
		GUEST, USER, ADMIN
	};

	private Authentication authentication;

	private boolean navbar = false;
	private int orderId = Integer.MAX_VALUE;

	public CoolPage(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}

	public boolean isNavbar() {
		return navbar;
	}

	public void setNavbar(boolean navbar) {
		this.navbar = navbar;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	@Override
	public String toString() {
		return "[COOLPAGE url: " + url + ", " + "authentication: "
				+ authentication + "," + "order-id: " + orderId + ", navbar: "
				+ navbar + "]";

	}
}
