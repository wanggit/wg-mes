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
package com.qcadoo.mes.workPlans.util;

import com.qcadoo.model.api.Entity;

import java.io.Serializable;
import java.util.Comparator;

public class OperationProductComponentComparator implements Comparator<Entity>, Serializable {

    private static final long serialVersionUID = 2985797934972953807L;

    private static final String L_PRODUCT = "product";

    private static final String L_NUMBER = "number";

    @Override
    public int compare(final Entity o1, final Entity o2) {
        return o1.getBelongsToField(L_PRODUCT).getStringField(L_NUMBER)
                .compareTo(o2.getBelongsToField(L_PRODUCT).getStringField(L_NUMBER));
    }

}