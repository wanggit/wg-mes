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
package com.qcadoo.mes.productionPerShift.hooks;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.dates.OrderDates;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.mes.productionPerShift.PpsTimeHelper;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.dataProvider.ProductionPerShiftDataProvider;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Service
public class OrderDetailsHooksPPS {

    private static final Predicate<Entity> HAS_DEFINED_PLANNED_START_DATE = order -> order.getDateField(OrderFields.DATE_FROM) != null;

    @Autowired
    private OrderDetailsRibbonHelper orderDetailsRibbonHelper;

    @Autowired
    private ProductionPerShiftDataProvider productionPerShiftDataProvider;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PpsTimeHelper ppsTimeHelper;

    public void onBeforeRender(final ViewDefinitionState view) {
        Predicate<Entity> technologyPredicate = OrderDetailsRibbonHelper.HAS_CHECKED_OR_ACCEPTED_TECHNOLOGY;
        orderDetailsRibbonHelper.setButtonEnabled(view, "orderProgressPlans", "productionPerShift", technologyPredicate,
                Optional.of("orders.ribbon.message.mustChangeTechnologyState"));
        orderDetailsRibbonHelper.setButtonEnabled(view, "orderProgressPlans", "productionPerShift", HAS_DEFINED_PLANNED_START_DATE,
                Optional.of("orders.ribbon.message.mustFillPlannedStartDate"));

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (form.getEntityId() != null) {
            checkOrderDates(view, form.getPersistedEntityWithIncludedFormValues());
        }
    }

    private void checkOrderDates(final ViewDefinitionState view, final Entity order) {
        if (order.getId() == null) {
            return;
        }
        Entity pps = productionPerShiftDataProvider
                .getProductionPerShiftDD()
                .find()
                .add(SearchRestrictions.belongsTo("order", OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER,
                        order.getId())).setMaxResults(1).uniqueResult();

        if (pps != null) {
            boolean shouldBeCorrected = OrderState.of(order).compareTo(OrderState.PENDING) != 0;
            Set<Long> progressForDayIds = productionPerShiftDataProvider.findIdsOfEffectiveProgressForDay(pps, shouldBeCorrected);
            DataDefinition progressForDayDD = dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                    ProductionPerShiftConstants.MODEL_PROGRESS_FOR_DAY);
            Optional<OrderDates> maybeOrderDates;
            try {
                maybeOrderDates = OrderDates.of(order);
            } catch (IllegalArgumentException e) {
                return;
            }
            DataDefinition orderDD = order.getDataDefinition();
            Entity dbOrder = orderDD.get(order.getId());
            boolean areDatesCorrect = true;
            if (maybeOrderDates.isPresent()) {
                OrderDates orderDates = maybeOrderDates.get();
                Date orderStart = removeTime(orderDates.getStart().effectiveWithFallback().toDate());
                Date orderEnd = orderDates.getEnd().effectiveWithFallback().toDate();
                Date ppsFinishDate = null;
                for (Long id : progressForDayIds) {
                    Entity progressForDay = progressForDayDD.get(id);
                    Date progressDate = progressForDay.getDateField(ProgressForDayFields.ACTUAL_DATE_OF_DAY);
                    if (progressDate == null) {
                        progressDate = progressForDay.getDateField(ProgressForDayFields.DATE_OF_DAY);
                    }
                    EntityList dailyProgresses = progressForDay.getHasManyField(ProgressForDayFields.DAILY_PROGRESS);
                    for (Entity dailyProgress : dailyProgresses) {

                        Date shiftFinishDate = ppsTimeHelper.findFinishDate(dailyProgress, progressDate, dbOrder);

                        if (shiftFinishDate == null) {
                            view.addMessage("productionPerShift.info.invalidStartDate", MessageType.INFO, false);
                            return;
                        }

                        if (ppsFinishDate == null || ppsFinishDate.before(shiftFinishDate)) {
                            ppsFinishDate = shiftFinishDate;
                        }

                        if (shiftFinishDate.before(orderStart)) {
                            areDatesCorrect = false;
                        }
                    }
                }
                if (ppsFinishDate != null) {
                    if (ppsFinishDate.after(orderEnd)) {
                        view.addMessage("productionPerShift.info.endDateTooLate", MessageType.INFO, false);
                    } else if (ppsFinishDate.before(orderEnd)) {
                        view.addMessage("productionPerShift.info.endDateTooEarly", MessageType.INFO, false);
                    }
                }
            }
            if (!areDatesCorrect) {
                view.addMessage("productionPerShift.info.invalidStartDate", MessageType.INFO, false);
            }
        }
    }

    private static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
