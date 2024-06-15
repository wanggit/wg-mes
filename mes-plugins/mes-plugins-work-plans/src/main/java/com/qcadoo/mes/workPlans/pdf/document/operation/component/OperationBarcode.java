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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.technologies.BarcodeOperationComponentService;
import com.qcadoo.model.api.Entity;

@Component
public class OperationBarcode {

    private BarcodeOperationComponentService barcodeOperationComponentService;

    @Autowired
    public OperationBarcode(final BarcodeOperationComponentService barcodeOperationComponentService) {
        this.barcodeOperationComponentService = barcodeOperationComponentService;
    }

    public void print(final PdfWriter pdfWriter, final Entity order, final Entity operationComponent, final Document document)
            throws DocumentException {
        PdfContentByte cb = pdfWriter.getDirectContent();
        Barcode128 code128 = new Barcode128();
        code128.setCode(barcodeOperationComponentService.getCodeFromBarcode(order, operationComponent));
        PdfPTable barcodeTable = new PdfPTable(1);
        barcodeTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        barcodeTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
        barcodeTable.getDefaultCell().setBorder(0);
        barcodeTable.setWidthPercentage(10f);
        barcodeTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        Image barcodeImage = code128.createImageWithBarcode(cb, null, null);
        barcodeTable.addCell(barcodeImage);

        document.add(barcodeTable);
    }

}
