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
package com.qcadoo.mes.productionCounting.validators;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.StaffWorkTimeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class StaffWorkTimeValidators {

    @Autowired
    private ParameterService parameterService;

    public boolean validatesWith(final DataDefinition staffWorkTimeDD, final Entity staffWorkTime) {

        if (!checkOperatorWorkTime(staffWorkTimeDD, staffWorkTime)) {
            return false;
        }

        Entity parameter = parameterService.getParameter();

        if (!parameter.getBooleanField(ParameterFieldsPC.ALLOW_MULTIPLE_REGISTERING_TIME_FOR_WORKER)) {
            Entity productionTracking = staffWorkTime.getBelongsToField(StaffWorkTimeFields.PRODUCTION_RECORD);

            Entity existingStaffWorkTime = productionTracking
                    .getHasManyField(ProductionTrackingFields.STAFF_WORK_TIMES)
                    .find()
                    .add(SearchRestrictions.belongsTo(StaffWorkTimeFields.WORKER,
                            staffWorkTime.getBelongsToField(StaffWorkTimeFields.WORKER))).setMaxResults(1).uniqueResult();
            if (staffWorkTime.getId() == null && existingStaffWorkTime != null && !existingStaffWorkTime.equals(staffWorkTime)) {
                staffWorkTime.addError(staffWorkTime.getDataDefinition().getField(StaffWorkTimeFields.WORKER),
                        "productionCounting.productionTracking.productionTrackingError.duplicateWorker");

                return false;
            }
        }

        return true;
    }

    private boolean checkOperatorWorkTime(final DataDefinition staffWorkTimeDD, final Entity staffWorkTime) {
        Date dateFrom = staffWorkTime.getDateField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_START);
        Date dateTo = staffWorkTime.getDateField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_END);

        if (dateFrom == null || dateTo == null || dateTo.after(dateFrom)) {
            return true;
        }
        staffWorkTime.addError(staffWorkTimeDD.getField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_END),
                "productionCounting.productionTracking.productionTrackingError.effectiveExecutionTimeEndBeforeEffectiveExecutionTimeStart");
        return false;
    }

}
