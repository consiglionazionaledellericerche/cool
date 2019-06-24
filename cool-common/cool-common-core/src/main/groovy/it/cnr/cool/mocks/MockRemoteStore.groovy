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

package it.cnr.cool.mocks

import it.cnr.cool.extensions.webscripts.RemoteStore

public class MockRemoteStore implements RemoteStore {

	private static final String JSON = "rbac.get.json.ftl";

	@Override
	public void updateDocument(String documentPath, String string)
			throws IOException {
		System.out.println("UNIMPLEMENTED");

	}

	@Override
	public InputStream getDocument(String documentPath)
			throws IOException {
		return MockRemoteStore.class.getResourceAsStream("/" + JSON);
	}

}

