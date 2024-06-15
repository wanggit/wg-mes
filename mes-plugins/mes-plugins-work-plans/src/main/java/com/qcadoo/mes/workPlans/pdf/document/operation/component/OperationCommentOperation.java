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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfHelper;

@Component
public class OperationCommentOperation {

    private final TranslationService translationService;

    private final PdfHelper pdfHelper;

    @Autowired
    public OperationCommentOperation(final TranslationService translationService, final PdfHelper pdfHelper) {
        this.pdfHelper = pdfHelper;
        this.translationService = translationService;
    }

    public void print(final Entity operationComponent, final Document document, final Locale locale) throws DocumentException {
        String commentContent = operationComponent.getStringField(TechnologyOperationComponentFields.COMMENT);

        if (Objects.isNull(commentContent)) {
            return;
        }

        PdfPTable table = pdfHelper.createPanelTable(1);
        table.getDefaultCell().setBackgroundColor(null);
        String commentLabel = translationService.translate("workPlans.workPlan.report.operation.comment", locale);
        pdfHelper.addTableCellAsOneColumnTable(table, commentLabel, commentContent);
        table.setSpacingAfter(18);
        table.setSpacingBefore(9);

        document.add(table);
    }

}
