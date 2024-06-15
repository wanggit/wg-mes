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
package com.qcadoo.mes.workPlans;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.workPlans.constants.ColumnForOrdersFields;
import com.qcadoo.mes.workPlans.constants.OrderSorting;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.mes.workPlans.pdf.document.operation.component.OperationProductHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;

@Service
public class WorkPlansServiceImpl implements WorkPlansService {

    private static final String PLANED_QUANTITY = "plannedQuantity";

    private static final String PLANED_QUANTIT_OPERATION = "plannedQuantityOperationProductColumn";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ParameterService parameterService;

    @Override
    public final Entity getWorkPlan(final Long workPlanId) {
        return getWorkPlanDD().get(workPlanId);
    }

    @Override
    public DataDefinition getWorkPlanDD() {
        return dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_WORK_PLAN);
    }

    @Override
    public final Entity getWorkPlanOrderColumn(final Long workPlanOrderColumnId) {
        return getWorkPlanOrderColumnDD().get(workPlanOrderColumnId);
    }

    @Override
    public DataDefinition getWorkPlanOrderColumnDD() {
        return dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_WORK_PLAN_ORDER_COLUMN);
    }

    @Override
    public final Entity getColumnForOrders(final Long columnForOrdersId) {
        return getColumnForOrdersDD().get(columnForOrdersId);
    }

    @Override
    public DataDefinition getColumnForOrdersDD() {
        return dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_FOR_ORDERS);
    }

    @Override
    public final Entity getColumnForInputProducts(final Long columnForInputProductsId) {
        return getColumnForInputProductsDD().get(columnForInputProductsId);
    }

    @Override
    public DataDefinition getColumnForInputProductsDD() {
        return dataDefinitionService
                .get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_COLUMN_FOR_INPUT_PRODUCTS);
    }

    @Override
    public final Entity getColumnForOutputProducts(final Long columnForOutputProductsId) {
        return getColumnForOutputProductsDD().get(columnForOutputProductsId);
    }

    @Override
    public DataDefinition getColumnForOutputProductsDD() {
        return dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER,
                WorkPlansConstants.MODEL_COLUMN_FOR_OUTPUT_PRODUCTS);
    }

    @Override
    public final Entity getParameterOrderColumn(final Long parameterOrderColumnId) {
        return getParameterOrderColumnDD().get(parameterOrderColumnId);
    }

    @Override
    public DataDefinition getParameterOrderColumnDD() {
        return dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_PARAMETER_ORDER_COLUMN);
    }

    @Override
    public final Entity getParameterInputColumn(final Long parameterInputColumnId) {
        return getParameterInputColumnDD().get(parameterInputColumnId);
    }

    @Override
    public DataDefinition getParameterInputColumnDD() {
        return dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_PARAMETER_INPUT_COLUMN);
    }

    @Override
    public final Entity getParameterOutputColumn(final Long parameterOutputColumnId) {
        return getParameterOutputColumnDD().get(parameterOutputColumnId);
    }

    @Override
    public DataDefinition getParameterOutputColumnDD() {
        return dataDefinitionService.get(WorkPlansConstants.PLUGIN_IDENTIFIER, WorkPlansConstants.MODEL_PARAMETER_OUTPUT_COLUMN);
    }

    @Override
    public final Entity generateWorkPlanEntity(final List<Entity> orders) {
        Entity workPlan = getWorkPlanDD().create();

        workPlan.setField(WorkPlanFields.NAME, generateNameForWorkPlan());
        workPlan.setField(WorkPlanFields.TYPE, WorkPlanType.NO_DISTINCTION.getStringValue());
        workPlan.setField(WorkPlanFields.ORDERS, orders);
        workPlan.setField(WorkPlanFields.GENERATED, false);

        workPlan.setField(WorkPlanFields.DONT_PRINT_ORDERS_IN_WORK_PLANS,
                parameterService.getParameter().getField(ParameterFieldsWP.DONT_PRINT_ORDERS_IN_WORK_PLANS));
        return workPlan.getDataDefinition().save(workPlan);
    }

    @Override
    public final List<Entity> getSelectedOrders(final Set<Long> selectedOrderIds) {
        List<Entity> orders = Lists.newArrayList();

        if (selectedOrderIds.isEmpty()) {
            return orders;
        }

        SearchCriteriaBuilder criteria = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find();
        criteria.add(SearchRestrictions.in("id", selectedOrderIds));

        SearchResult result = criteria.list();

        if (result.getTotalNumberOfEntities() == 0) {
            return orders;
        }

        orders.addAll(result.getEntities());

        return orders;
    }

    public String generateNameForWorkPlan() {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, LocaleContextHolder.getLocale());

        return translationService.translate("workPlans.workPlan.defaults.name", LocaleContextHolder.getLocale(),
                dateFormat.format(currentDate));
    }

    @Override
    public final boolean checkAttachmentExtension(final DataDefinition dataDefinition, final FieldDefinition attachmentFieldDef,
            final Entity entity, final Object oldValue, final Object newValue) {
        if (StringUtils.equals((String) oldValue, (String) newValue) || checkAttachmentExtension((String) newValue)) {
            return true;
        }
        entity.addError(attachmentFieldDef, "workPlans.imageUrlInWorkPlan.message.attachmentExtensionIsNotValid");
        return false;
    }

    private boolean checkAttachmentExtension(final String attachementPathValue) {
        if (StringUtils.isBlank(attachementPathValue)) {
            return true;
        }

        // TODO DEV_TEAM after upgrade Apache's commons-lang this loop
        // may be replaced with StringUtils.endsWithAny(attachementPathValue.toLowerCase(), WorkPlansConstants.FILE_EXTENSIONS)
        for (String allowedFileExtension : WorkPlansConstants.FILE_EXTENSIONS) {
            if (StringUtils.endsWithIgnoreCase(attachementPathValue, '.' + allowedFileExtension)) {
                return true;
            }
        }

        return false;
    }

    @Override
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
                    for (Entity modelComponent : model.getHasManyField(componentName)) {
                        Entity columnUsed = modelComponent.getBelongsToField(columnName);

                        if (columnUsed.getId().equals(column.getId())) {
                            component.addError(componentDD.getField(columnName), "workPlans.column.message.columnIsAlreadyUsed");

                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public int getAdditionalRowsFromParameter(String field) {
        Integer rows = parameterService.getParameter().getIntegerField(field);
        return rows != null ? rows : 0;
    }

    public void workPlanDelivered(final ComponentState state, List<Entity> selectedEntities) {
        for (Entity orderListDtoEntity : selectedEntities) {
            boolean isWorkPlanDelivered = orderListDtoEntity.getBooleanField(ColumnForOrdersFields.WORK_PLAN_DELIVERED);
            if (!isWorkPlanDelivered) {
                DataDefinition orderDD = dataDefinitionService
                        .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
                Entity order = orderDD.get(orderListDtoEntity.getId());
                orderListDtoEntity.setField(ColumnForOrdersFields.WORK_PLAN_DELIVERED, true);
                order.setField(ColumnForOrdersFields.WORK_PLAN_DELIVERED, true);
                orderDD.save(order);
            }
        }

        state.addMessage("workPlans.workPlanDelivered.message.generated", ComponentState.MessageType.SUCCESS);
    }

    @Override
    public List<OperationProductHelper> sortByColumn(Entity workPlan, List<OperationProductHelper> operationProductsValue,
            List<String> headers) {
        Entity columnIdentifier = workPlan.getBelongsToField(WorkPlanFields.INPUT_PRODUCT_COLUMN_TO_SORT_BY);
        if (Objects.isNull(columnIdentifier)) {
            return operationProductsValue;
        }
        columnIdentifier = columnIdentifier.getDataDefinition().get(columnIdentifier.getId());
        String translatedName = translationService.translate(columnIdentifier.getStringField("name"),
                LocaleContextHolder.getLocale());
        if (StringUtils.isEmpty(workPlan.getStringField(WorkPlanFields.ORDER_SORTING)) || !headers.contains(translatedName)) {
            return operationProductsValue;
        }
        final String identifier = columnIdentifier.getStringField("identifier");

        Collections.sort(operationProductsValue, new Comparator<OperationProductHelper>() {

            @Override
            public int compare(OperationProductHelper o1, OperationProductHelper o2) {
                if (o1 == null || o2 == null) {
                    return 0;
                }

                if (PLANED_QUANTITY.equals(identifier) || PLANED_QUANTIT_OPERATION.equals(identifier)) {
                    return new BigDecimal(o1.getOperationProductColumnHelpers().stream()
                            .filter(oc -> oc.getIdentifier().equals(identifier)).findAny().get().getValue().replace(",", "."))
                            .compareTo(new BigDecimal(o2.getOperationProductColumnHelpers().stream()
                                    .filter(oc -> oc.getIdentifier().equals(identifier)).findAny().get().getValue()
                                    .replace(",", ".")));
                }

                return o1
                        .getOperationProductColumnHelpers()
                        .stream()
                        .filter(oc -> oc.getIdentifier().equals(identifier))
                        .findAny()
                        .get()
                        .getValue()
                        .compareTo(
                                o2.getOperationProductColumnHelpers().stream()
                                        .filter(oc -> oc.getIdentifier().equals(identifier)).findAny().get().getValue());
            }
        });

        if (OrderSorting.ASC.getStringValue().equals(workPlan.getStringField(WorkPlanFields.ORDER_SORTING))) {
            return operationProductsValue;
        }
        Collections.reverse(operationProductsValue);

        return operationProductsValue;
    }

}
