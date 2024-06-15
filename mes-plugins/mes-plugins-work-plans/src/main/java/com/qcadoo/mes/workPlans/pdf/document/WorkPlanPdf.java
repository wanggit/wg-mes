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
package com.qcadoo.mes.workPlans.pdf.document;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.workPlans.pdf.document.component.DocumentHeader;
import com.qcadoo.mes.workPlans.pdf.document.component.OperationSection;
import com.qcadoo.mes.workPlans.pdf.document.component.OrderSection;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.model.api.Entity;

@Component
public class WorkPlanPdf {

    private DocumentHeader documentHeader;

    private OperationSection operationSection;

    private OrderSection orderSection;

    @Autowired
    public WorkPlanPdf(final DocumentHeader documentHeader, final OperationSection operationSection,
            final OrderSection orderSection) {
        this.documentHeader = documentHeader;
        this.operationSection = operationSection;
        this.orderSection = orderSection;
    }

    public void print(final PdfWriter pdfWriter, final GroupingContainer groupingContainer, final Entity workPlan,
            final Document document, final Locale locale) throws DocumentException {
        documentHeader.print(workPlan, document, locale);
        orderSection.print(workPlan, groupingContainer, document, locale);
        operationSection.print(workPlan, pdfWriter, groupingContainer, document, locale);
    }

}
