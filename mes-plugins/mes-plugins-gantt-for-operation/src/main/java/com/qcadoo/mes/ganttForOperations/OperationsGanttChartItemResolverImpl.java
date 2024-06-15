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
package com.qcadoo.mes.ganttForOperations;

import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.constants.OperationTimeCalculationsConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.operationTimeCalculations.constants.OrderTimeCalculationFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.view.api.components.ganttChart.GanttChartItem;
import com.qcadoo.view.api.components.ganttChart.GanttChartScale;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class OperationsGanttChartItemResolverImpl implements OperationsGanttChartItemResolver {

    private static final String NAME_FIELD = "name";

    private static final String NUMBER_FIELD = "number";

    private static final String NODE_NUMBER_FIELD = "nodeNumber";

    private static final String ORDERS_MODEL = "orders";

    private static final String OPERATION_FIELD = "operation";

    private static final String ORDER_FIELD = "order";

    private static final String EFFECTIVE_DATE_TO_FIELD = "effectiveDateTo";

    private static final String EFFECTIVE_DATE_FROM_FIELD = "effectiveDateFrom";

    @Autowired
    private EntityTreeUtilsService entityTreeUtilsService;

    private static final Logger LOG = LoggerFactory.getLogger(OperationsGanttChartItemResolverImpl.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Override
    public Map<String, List<GanttChartItem>> resolve(final GanttChartScale scale, final JSONObject context, final Locale locale) {
        try {
            Long orderId = Long.valueOf(context.getString("orderId"));
            Entity order = dataDefinitionService.get(ORDERS_MODEL, ORDER_FIELD).get(orderId);

            if (order == null) {
                LOG.warn("Cannot find order for " + orderId);
                return Collections.emptyMap();
            }

            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

            List<Entity> operations = dataDefinitionService
                    .get(com.qcadoo.mes.technologies.constants.TechnologiesConstants.PLUGIN_IDENTIFIER,
                            com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                    .find().add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology)).list()
                    .getEntities();

            if (operations.isEmpty()) {
                LOG.warn("Cannot find operations for " + order);
                return Collections.emptyMap();
            }

            if (scale.getIsDatesSet() != null && scale.getIsDatesSet()) {
                Entity orderTimeCalculation = dataDefinitionService.get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER, OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION).find()
                        .add(SearchRestrictions.belongsTo("order", order)).setMaxResults(1).uniqueResult();
                if (Objects.nonNull(orderTimeCalculation)) {
                    scale.setDateFrom(orderTimeCalculation.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_FROM));

                    scale.setDateTo(orderTimeCalculation.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_TO));
                }
            }
            Map<String, List<GanttChartItem>> items = new LinkedHashMap<String, List<GanttChartItem>>();
            Map<String, Integer> counters = new HashMap<String, Integer>();

            List<Entity> sortedOperationFromTree = entityTreeUtilsService.getSortedEntities(order.getBelongsToField(
                    OrderFields.TECHNOLOGY).getTreeField(TechnologyFields.OPERATION_COMPONENTS));

            for (Entity operationFromTree : sortedOperationFromTree) {
                Entity operation = operations.get(operations.indexOf(operationFromTree));

                Entity timeCalculation = operationWorkTimeService.createOrGetOperCompTimeCalculation(order, operation);

                Date dateFrom = timeCalculation.getDateField(EFFECTIVE_DATE_FROM_FIELD);
                Date dateTo = timeCalculation.getDateField(EFFECTIVE_DATE_TO_FIELD);

                if (dateFrom == null || dateTo == null || dateTo.before(scale.getDateFrom())) {
                    continue;
                }

                StringBuffer operationName = new StringBuffer(getDescriptionForOperation(operation));

                int counter = 0;

                if (counters.containsKey(operationName.toString())) {
                    counter = counters.get(operationName.toString()) + 1;
                    operationName.append(" (");
                    operationName.append(counter);
                    operationName.append(") ");
                }

                GanttChartItem item = scale.createGanttChartItem(operationName.toString(), operationName.toString(),
                        operation.getId(), dateFrom, dateTo);

                if (item != null) {
                    items.put(operationName.toString(), Collections.singletonList(item));
                    counters.put(operationName.toString(), counter);
                }
            }

            return items;
        } catch (NumberFormatException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (JSONException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String getDescriptionForOperation(final Entity operation) {
        return operation.getStringField(NODE_NUMBER_FIELD) + " "
                + operation.getBelongsToField(OPERATION_FIELD).getStringField(NUMBER_FIELD) + " "
                + operation.getBelongsToField(OPERATION_FIELD).getStringField(NAME_FIELD);
    }

}
