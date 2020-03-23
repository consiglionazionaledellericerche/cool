/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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

package it.cnr.cool.security.service.impl.alfresco;

public class Paging {
    private Long maxItems;
    private Long skipCount;
    private Long totalItems;
    private Long totalItemsRangeEnd;

    public Paging() {
    }

    public Long getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(Long maxItems) {
        this.maxItems = maxItems;
    }

    public Long getSkipCount() {
        return skipCount;
    }

    public void setSkipCount(Long skipCount) {
        this.skipCount = skipCount;
    }

    public Long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Long totalItems) {
        this.totalItems = totalItems;
    }

    public Long getTotalItemsRangeEnd() {
        return totalItemsRangeEnd;
    }

    public void setTotalItemsRangeEnd(Long totalItemsRangeEnd) {
        this.totalItemsRangeEnd = totalItemsRangeEnd;
    }
}
