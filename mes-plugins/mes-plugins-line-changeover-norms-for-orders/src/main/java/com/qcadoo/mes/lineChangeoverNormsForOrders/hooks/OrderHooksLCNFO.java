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
package com.qcadoo.mes.lineChangeoverNormsForOrders.hooks;

import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderHooksLCNFO {

    @Autowired
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    public final boolean checkIfOrderHasCorrectStateAndIsPrevious(final DataDefinition orderDD, final Entity order) {
        Entity previousOrderDB = order.getBelongsToField(OrderFieldsLCNFO.PREVIOUS_ORDER);
        Entity orderDB = order.getBelongsToField(OrderFieldsLCNFO.ORDER);

        if (!lineChangeoverNormsForOrdersService.previousOrderEndsBeforeOrIsWithdrawed(previousOrderDB, orderDB)) {
            order.addError(orderDD.getField(OrderFieldsLCNFO.PREVIOUS_ORDER),
                    "orders.order.previousOrder.message.orderIsIncorrect");

            return false;
        }

        return true;
    }

    public void onSave(final DataDefinition orderDD, final Entity order) {
        if (!order.getBooleanField(OrderFieldsLCNFO.OWN_LINE_CHANGEOVER)) {
            order.setField(OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION, null);
        }
    }

}
