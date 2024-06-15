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

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.dates.ProgressDatesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionPerShiftHooks {

    @Autowired
    private ProgressDatesService progressDatesService;

    public void onSave(final DataDefinition dataDefinition, final Entity pps) {
        Optional<Entity> maybeOrder = Optional.ofNullable(pps.getBelongsToField(ProductionPerShiftFields.ORDER));
        for (Entity order : maybeOrder.map(Collections::singleton).orElse(Collections.emptySet())) {
            progressDatesService.setUpDatesFor(order);
        }
        fillOrderFinishDate(dataDefinition, pps);
    }

    public void fillOrderFinishDate(final DataDefinition ppsDD, final Entity pps) {
        Entity order = pps.getBelongsToField(ProductionPerShiftFields.ORDER);
        Date orderFinishDate = pps.getDateField(ProductionPerShiftFields.ORDER_FINISH_DATE);
        if (orderFinishDate != null) {
            boolean shouldBeCorrected = OrderState.of(order).compareTo(OrderState.PENDING) != 0;

            order.setField(OrderFields.FINISH_DATE, orderFinishDate);
            if (shouldBeCorrected) {
                order.setField(OrderFields.CORRECTED_DATE_TO, orderFinishDate);
            }
            pps.setField(ProductionPerShiftFields.ORDER_FINISH_DATE, null);

            order.getDataDefinition().save(order);
        }
    }

}
