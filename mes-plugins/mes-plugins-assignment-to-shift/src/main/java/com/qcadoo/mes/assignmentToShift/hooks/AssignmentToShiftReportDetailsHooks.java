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
package com.qcadoo.mes.assignmentToShift.hooks;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftReportFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public final class AssignmentToShiftReportDetailsHooks {

    private static final List<String> L_REPORT_FIELDS = Arrays.asList(AssignmentToShiftReportFields.NUMBER,
            AssignmentToShiftReportFields.NAME, AssignmentToShiftReportFields.SHIFT, AssignmentToShiftReportFields.FACTORY,
            AssignmentToShiftReportFields.DATE_FROM, AssignmentToShiftReportFields.DATE_TO);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void generateAssignmentToShiftReportNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, AssignmentToShiftConstants.PLUGIN_IDENTIFIER,
                AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT_REPORT, QcadooViewConstants.L_FORM,
                AssignmentToShiftReportFields.NUMBER);
    }

    public void disableFields(final ViewDefinitionState view) {
        FormComponent assignmentToShiftReportForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long assignmentToShiftReportId = assignmentToShiftReportForm.getEntityId();

        if (Objects.isNull(assignmentToShiftReportId)) {
            setFieldsState(view, L_REPORT_FIELDS, true);
        } else {
            Entity assignmentToShiftReport = getAssignmentToShiftReportFromDB(assignmentToShiftReportId);

            if (Objects.nonNull(assignmentToShiftReport)) {
                boolean generated = assignmentToShiftReport.getBooleanField(AssignmentToShiftReportFields.GENERATED);

                setFieldsState(view, L_REPORT_FIELDS, !generated);
            }
        }
    }

    private void setFieldsState(final ViewDefinitionState view, final List<String> fieldNames, final boolean enabled) {
        for (String fieldName : fieldNames) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(fieldName);

            fieldComponent.setEnabled(enabled);
            fieldComponent.requestComponentUpdateState();
        }
    }

    private Entity getAssignmentToShiftReportFromDB(final Long assignmentToShiftReportId) {
        return dataDefinitionService
                .get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER, AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT_REPORT)
                .get(assignmentToShiftReportId);
    }

}
