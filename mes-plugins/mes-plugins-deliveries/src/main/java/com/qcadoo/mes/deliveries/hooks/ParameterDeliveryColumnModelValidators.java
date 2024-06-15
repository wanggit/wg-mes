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
package com.qcadoo.mes.deliveries.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterDeliveryColumnModelValidators {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final boolean checkIfColumnForOrdersIsNotAlreadyUsed(final DataDefinition ordersColumnDD, final Entity ordersColumn) {
        return checkIfColumnIsNotUsed(ordersColumnDD, ordersColumn, "parameter", "columnForOrders",
                "parameterDeliveryOrderColumn");

    }

    public boolean checkIfColumnIsNotUsed(final DataDefinition componentDD, final Entity component, final String modelName,
            final String columnName, final String componentName) {

        if (component.getId() == null) {
            Entity column = component.getBelongsToField(columnName);

            if (column == null) {
                return true;
            } else {
                Entity model = component.getBelongsToField(modelName);

                if (model == null) {
                    return true;
                } else {
                    for (Entity columnUsed : dataDefinitionService
                            .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_PARAMETER_DELIVERY_ORDER_COLUMN)
                            .find().list().getEntities()) {

                        if (columnUsed.getBelongsToField("columnForOrders").getId().equals(column.getId())) {
                            component.addError(componentDD.getField(columnName), "deliveries.column.message.columnIsAlreadyUsed");

                            return false;
                        }
                    }

                }
            }
        }

        return true;
    }
}
