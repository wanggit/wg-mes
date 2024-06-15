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
package com.qcadoo.mes.deliveries;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.columnExtension.ColumnExtensionService;
import com.qcadoo.mes.columnExtension.constants.OperationType;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class DeliveriesColumnLoaderServiceImpl implements DeliveriesColumnLoaderService {

    private static final Logger LOG = LoggerFactory.getLogger(DeliveriesColumnLoaderServiceImpl.class);

    private static final String L_COLUMN_FOR_DELIVERIES = "columnForDeliveries";

    private static final String L_COLUMN_FOR_ORDERS = "columnForOrders";

    @Autowired
    private ColumnExtensionService columnExtensionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void fillColumnsForDeliveries(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_DELIVERIES);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_DELIVERIES, OperationType.ADD, columnAttributes);
        }
    }

    @Override
    public void clearColumnsForDeliveries(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_DELIVERIES);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_DELIVERIES, OperationType.DELETE, columnAttributes);
        }
    }

    @Override
    public void fillColumnsForOrders(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_ORDERS);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_ORDERS, OperationType.ADD, columnAttributes);
        }
    }

    @Override
    public void clearColumnsForOrders(final String plugin) {
        Map<Integer, Map<String, String>> columnsAttributes = columnExtensionService.getColumnsAttributesFromXML(plugin,
                L_COLUMN_FOR_ORDERS);

        for (Map<String, String> columnAttributes : columnsAttributes.values()) {
            readData(L_COLUMN_FOR_ORDERS, OperationType.DELETE, columnAttributes);
        }
    }

    private void readData(final String type, final OperationType operation, final Map<String, String> values) {
        if (L_COLUMN_FOR_DELIVERIES.equals(type)) {
            if (OperationType.ADD.equals(operation)) {
                addColumnForDeliveries(values);
            } else if (OperationType.DELETE.equals(operation)) {
                deleteColumnForDeliveries(values);
            }
        } else if (L_COLUMN_FOR_ORDERS.equals(type)) {
            if (OperationType.ADD.equals(operation)) {
                addColumnForOrders(values);
            } else if (OperationType.DELETE.equals(operation)) {
                deleteColumnForOrders(values);
            }
        }
    }

    private void addColumnForDeliveries(final Map<String, String> columnAttributes) {
        Entity column = columnExtensionService.addColumn(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES, columnAttributes);
        Entity parameter = parameterService.getParameter();
        column.setField("parameter", parameter);
        column.getDataDefinition().save(column);
    }

    private void deleteColumnForDeliveries(final Map<String, String> columnAttributes) {
        columnExtensionService.deleteColumn(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES, columnAttributes);
    }

    private void addColumnForOrders(final Map<String, String> columnAttributes) {
        Entity column = columnExtensionService.addColumn(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_COLUMN_FOR_ORDERS, columnAttributes);
        Entity parameter = parameterService.getParameter();
        column.setField("parameter", parameter);
        column.getDataDefinition().save(column);

        addColumnForParameterOrders(column);

    }

    private void addColumnForParameterOrders(final Entity columnForOrders) {
        Entity parameterOrderColumn = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_PARAMETER_DELIVERY_ORDER_COLUMN).create();
        parameterOrderColumn.setField(BasicConstants.MODEL_PARAMETER, parameterService.getParameter());
        parameterOrderColumn.setField(DeliveriesConstants.MODEL_COLUMN_FOR_ORDERS, columnForOrders);
        parameterOrderColumn = parameterOrderColumn.getDataDefinition().save(parameterOrderColumn);

        if (parameterOrderColumn.isValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Column added to parameter orders columns {column=" + parameterOrderColumn.toString() + "}");
            }
        } else {
            throw new IllegalStateException("Saved entity - parameterOrdersColumn - has validation errors - "
                    + columnForOrders.toString());
        }
    }

    private void deleteColumnForOrders(final Map<String, String> columnAttributes) {
        columnExtensionService.deleteColumn(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COLUMN_FOR_ORDERS,
                columnAttributes);
    }

    public boolean isColumnsForDeliveriesEmpty() {
        return columnExtensionService.isColumnsEmpty(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES);
    }

    public boolean isColumnsForOrdersEmpty() {
        return columnExtensionService.isColumnsEmpty(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_COLUMN_FOR_ORDERS);
    }

}
