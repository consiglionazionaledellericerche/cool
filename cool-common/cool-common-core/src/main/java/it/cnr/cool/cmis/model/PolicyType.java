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

package it.cnr.cool.cmis.model;

import java.util.ArrayList;
import java.util.List;

public enum PolicyType {
	INCOMPLETE_ASPECT("P:sys:incomplete", "sys:incomplete");

	
    private final String value;
    private final String queryName;
	public static String ASPECT_REQ_PARAMETER_NAME = "aspect";
	public static String ADD_REMOVE_ASPECT_REQ_PARAMETER_NAME = "add-remove-aspect";

    PolicyType(String v, String queryName) {
        value = v;
        this.queryName = queryName;
    }

    public String value() {
        return value;
    }

    public String queryName() {
        return queryName;
    }

    public static PolicyType fromValue(String v) {
        for (PolicyType c : PolicyType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
    
    public static List<String> getAspectToBeAdd(String[] aspects){
    	List<String> results = new ArrayList<String>();
    	if (aspects != null)
	    	for (int i = 0; i < aspects.length; i++) {
				String name = aspects[i];
				if (name.startsWith("add-"))
					results.add(name.substring(4));
			}
    	return results;
    }
    
    public static List<String> getAspectToBeRemoved(String[] aspects){
    	List<String> results = new ArrayList<String>();
        if (aspects != null) {
            for (int i = 0; i < aspects.length; i++) {
                String name = aspects[i];
                if (name.startsWith("remove-"))
                    results.add(name.substring(7));
            }
        }
    	return results;
    }

}
