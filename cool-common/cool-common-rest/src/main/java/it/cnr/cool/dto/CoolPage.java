package it.cnr.cool.dto;

public class CoolPage {

	private final String url;

	public enum Authentication {
		GUEST, USER, ADMIN
	};

	private Authentication authentication;

	private int orderId = Integer.MAX_VALUE;

	private String formatId = null;

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

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public String getFormatId() {
		return formatId;
	}

	public void setFormatId(String formatId) {
		this.formatId = formatId;
	}

	@Override
	public String toString() {
		return "[COOLPAGE url: " + url + ", " + "authentication: "
				+ authentication + "," + "order-id: " + orderId + ", navbar: "
				+ formatId + "]";

	}
}
