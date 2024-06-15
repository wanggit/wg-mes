/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.costNormsForOperation.constants;

import java.util.Set;

import com.google.common.collect.Sets;

public final class CostNormsForOperationConstants {

    private CostNormsForOperationConstants() {

    }

    public static final String PLUGIN_IDENTIFIER = "costNormsForOperation";

    // MODEL
    public static final String MODEL_CALCULATION_OPERATION_COMPONENT = "calculationOperationComponent";

    public static final Set<String> FIELDS = Sets.newHashSet("laborHourlyCost",
            "machineHourlyCost");

}
