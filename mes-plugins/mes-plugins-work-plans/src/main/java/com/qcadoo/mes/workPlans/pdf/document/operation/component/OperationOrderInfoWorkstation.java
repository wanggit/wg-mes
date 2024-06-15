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
package com.qcadoo.mes.workPlans.pdf.document.operation.component;

import java.util.Locale;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfHelper;

@Component
public class OperationOrderInfoWorkstation {

    private TranslationService translationService;

    private PdfHelper pdfHelper;

    @Autowired
    public OperationOrderInfoWorkstation(final TranslationService translationService, final PdfHelper pdfHelper) {
        this.translationService = translationService;
        this.pdfHelper = pdfHelper;
    }

    public void print(final Entity operationComponent, final PdfPTable operationTable, final Locale locale) {
        Entity workstationType = extractWorkstationTypeFromToc(operationComponent);

        String workstationTypeName = "";
        String divisionName = "";
        String supervisorName = "";
        String divisionLabel = "";
        String supervisorLabel = "";

        if (Objects.nonNull(workstationType)) {
            workstationTypeName = workstationType.getStringField(WorkstationTypeFields.NAME);

            pdfHelper.addTableCellAsOneColumnTable(operationTable,
                    translationService.translate("workPlans.workPlan.report.operation.workstationType", locale),
                    workstationTypeName);
            pdfHelper.addTableCellAsOneColumnTable(operationTable, divisionLabel, divisionName);
            pdfHelper.addTableCellAsOneColumnTable(operationTable, supervisorLabel, supervisorName);
        }
    }

    private Entity extractWorkstationTypeFromToc(final Entity operationComponent) {
        return operationComponent.getBelongsToField(TechnologyOperationComponentFields.WORKSTATION_TYPE);
    }

}
