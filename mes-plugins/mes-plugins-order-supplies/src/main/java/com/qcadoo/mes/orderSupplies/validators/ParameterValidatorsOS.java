/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.orderSupplies.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orderSupplies.constants.ParameterFieldsOS;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterValidatorsOS {

    public boolean validatesWith(final DataDefinition parameterDD, final Entity parameter) {
        return checkIfOrdersIncludePeriodFilled(parameterDD, parameter);
    }

    private boolean checkIfOrdersIncludePeriodFilled(final DataDefinition parameterDD, final Entity parameter) {
        boolean includeRequirements = parameter.getBooleanField(ParameterFieldsOS.INCLUDE_REQUIREMENTS);

        if (includeRequirements) {
            Integer ordersIncludePeriod = parameter.getIntegerField(ParameterFieldsOS.ORDERS_INCLUDE_PERIOD);
            if (ordersIncludePeriod == null || ordersIncludePeriod.compareTo(0) < 0) {
                parameter.addError(parameterDD.getField(ParameterFieldsOS.ORDERS_INCLUDE_PERIOD),
                        "basic.parameter.ordersInludePeriod.isSmallerThanOne");

                return false;
            }
        }

        return true;
    }
}
