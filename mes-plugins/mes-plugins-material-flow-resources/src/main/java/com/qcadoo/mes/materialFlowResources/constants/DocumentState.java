/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.constants;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;

public enum DocumentState {

    DRAFT("01draft"), ACCEPTED("02accepted");

    private final String value;

    private DocumentState(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return this.value;
    }

    public static DocumentState of(final Entity entity) {
        Preconditions.checkArgument(entity != null, "Passed entity have to be non null");
        return parseString(entity.getStringField(DocumentFields.STATE));
    }

    public static DocumentState parseString(final String type) {
        for (DocumentState documentState : DocumentState.values()) {
            if (StringUtils.equalsIgnoreCase(type, documentState.getStringValue())) {
                return documentState;
            }
        }

        throw new IllegalArgumentException("Couldn't parse DocumentState from string '" + type + "'");
    }

}
