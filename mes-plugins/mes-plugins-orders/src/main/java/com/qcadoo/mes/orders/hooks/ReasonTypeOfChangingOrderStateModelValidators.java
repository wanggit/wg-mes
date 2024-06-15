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
package com.qcadoo.mes.orders.hooks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.ReasonTypeOfChangingOrderStateFields;
import com.qcadoo.mes.orders.states.constants.OrderStateChangeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Service
public class ReasonTypeOfChangingOrderStateModelValidators {

    public boolean checkReasonRequired(final DataDefinition reasonTypeOfChangingOrderStateDD,
            final FieldDefinition fieldDefinition, final Entity reasonTypeOfChangingOrderState, final Object oldValue,
            final Object newValue) {
        Entity orderStateChange = reasonTypeOfChangingOrderState
                .getBelongsToField(ReasonTypeOfChangingOrderStateFields.ORDER_STATE_CHANGE);
        if (orderStateChange.getBooleanField(OrderStateChangeFields.REASON_REQUIRED) && StringUtils.isEmpty((String) newValue)) {
            reasonTypeOfChangingOrderState.addError(fieldDefinition, fieldDefinition.getName(),
                    "qcadooView.validate.field.error.missing");
            return false;
        }
        return true;
    }
}
