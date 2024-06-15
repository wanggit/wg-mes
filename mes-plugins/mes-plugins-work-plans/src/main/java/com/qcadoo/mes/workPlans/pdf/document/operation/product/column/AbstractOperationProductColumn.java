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
package com.qcadoo.mes.workPlans.pdf.document.operation.product.column;

import java.util.Locale;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.ProductDirection;

public abstract class AbstractOperationProductColumn implements OperationProductColumn {

    private TranslationService translationService;

    public AbstractOperationProductColumn(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public String getName(Locale locale, ProductDirection productDirection) {
        if (ProductDirection.OUT.equals(productDirection)) {
            return translationService.translate("workPlans.columnForOutputProducts.name.value." + getIdentifier(), locale);
        } else if (ProductDirection.IN.equals(productDirection)) {
            return translationService.translate("workPlans.columnForInputProducts.name.value." + getIdentifier(), locale);
        }
        return null;
    }

    @Override
    public String getDescription(Locale locale, ProductDirection productDirection) {
        if (ProductDirection.OUT.equals(productDirection)) {
            return translationService.translate("workPlans.columnForOutputProducts.description.value." + getIdentifier(), locale);
        } else if (ProductDirection.IN.equals(productDirection)) {
            return translationService.translate("workPlans.columnForInputProducts.description.value." + getIdentifier(), locale);
        }
        return null;
    }

}
