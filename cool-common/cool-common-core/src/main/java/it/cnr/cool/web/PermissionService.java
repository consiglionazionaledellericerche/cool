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

package it.cnr.cool.web;


import java.util.List;

public interface PermissionService {
	public static enum methods {GET, POST, PUT, DELETE};
	public static enum lists {whitelist, blacklist};
	public static enum types {all, group, user};

	boolean add(String id, methods method, lists list, types type, String authority);
	boolean delete(String id, methods method, lists list, types type, String authority);
    String getRbacAsString();
    boolean isAuthorized(String id, String method, String username, List<String> groups);
}
