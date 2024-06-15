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
package com.qcadoo.mes.productionPerShift.util;

import com.google.common.base.Optional;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftFields;
import com.qcadoo.mes.productionPerShift.dataProvider.ProductionPerShiftDataProvider;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProgressQuantitiesDeviationNotifier {

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionPerShiftDataProvider productionPerShiftDataProvider;

    public void compareAndNotify(final ViewDefinitionState view, final Entity pps, boolean shouldBeCorrected) {
        Optional<BigDecimal> maybeQuantitiesDifference = calculateQuantitiesDifference(pps, shouldBeCorrected);
        for (BigDecimal quantitiesDifference : maybeQuantitiesDifference.asSet()) {
            int compareResult = quantitiesDifference.compareTo(BigDecimal.ZERO);
            if (compareResult > 0) {
                showQuantitiesDeviationNotice(view, quantitiesDifference,
                        "productionPerShift.productionPerShiftDetails.sumPlanedQuantityPSSmaller");
            } else if (compareResult < 0) {
                showQuantitiesDeviationNotice(view, quantitiesDifference,
                        "productionPerShift.productionPerShiftDetails.sumPlanedQuantityPSGreater");
            }
        }
    }

    private Optional<BigDecimal> calculateQuantitiesDifference(final Entity pps, boolean shouldBeCorrected) {

        Entity order = pps.getBelongsToField(ProductionPerShiftFields.ORDER);

        BigDecimal sumOfDailyPlannedQuantities = productionPerShiftDataProvider.countSumOfQuantities(pps, shouldBeCorrected);
        BigDecimal planedQuantityFromOrder = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
        return Optional.of(planedQuantityFromOrder.subtract(sumOfDailyPlannedQuantities, numberService.getMathContext()));
    }

    private void showQuantitiesDeviationNotice(final ViewDefinitionState view, final BigDecimal quantitiesDifference,
            final String messageKey) {
        for (ComponentState productionPerShiftForm : view.tryFindComponentByReference(QcadooViewConstants.L_FORM).asSet()) {
            productionPerShiftForm.addMessage(messageKey, ComponentState.MessageType.INFO, false,
                    numberService.formatWithMinimumFractionDigits(quantitiesDifference.abs(numberService.getMathContext()), 0));
        }
    }

}
