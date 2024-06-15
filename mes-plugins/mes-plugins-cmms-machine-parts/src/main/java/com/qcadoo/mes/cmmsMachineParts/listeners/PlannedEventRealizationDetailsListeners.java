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
package com.qcadoo.mes.cmmsMachineParts.listeners;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventRealizationFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PlannedEventRealizationDetailsListeners {

    public void calculateDuration(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent startDateFieldComponent = (FieldComponent) view
                .getComponentByReference(PlannedEventRealizationFields.START_DATE);
        FieldComponent finishDateFieldComponent = (FieldComponent) view
                .getComponentByReference(PlannedEventRealizationFields.FINISH_DATE);
        FieldComponent durationFieldComponent = (FieldComponent) view
                .getComponentByReference(PlannedEventRealizationFields.DURATION);

        if ((startDateFieldComponent.getFieldValue() == null || startDateFieldComponent.getFieldValue().toString().isEmpty())
                || (finishDateFieldComponent.getFieldValue() == null || finishDateFieldComponent.getFieldValue().toString().isEmpty()) ) {
            return;
        }

        Date start = DateUtils.parseDate(startDateFieldComponent.getFieldValue());
        Date end = DateUtils.parseDate(finishDateFieldComponent.getFieldValue());

        if (start != null && end != null && start.before(end)) {
            Seconds seconds = Seconds.secondsBetween(new DateTime(start), new DateTime(end));
            durationFieldComponent.setFieldValue(Integer.valueOf(seconds.getSeconds()));
        }
        durationFieldComponent.requestComponentUpdateState();
    }
}
