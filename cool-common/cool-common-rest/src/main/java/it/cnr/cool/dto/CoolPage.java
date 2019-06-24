/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
