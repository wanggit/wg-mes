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
package com.qcadoo.mes.basic.validators;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.plugin.api.PluginUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductValidators {

    @Autowired
    private ParameterService parameterService;

    public boolean checkEanUniqueness(final DataDefinition productDD, final FieldDefinition eanFieldDefinition,
            final Entity product, final Object eanOldValue, final Object eanNewValue) {

        if(!PluginUtils.isEnabled("urcBasic") && parameterService.getParameter().getBooleanField(ParameterFields.MANY_ARTICLES_WITH_THE_SAME_EAN)) {
            return true;
        }

        String ean = (String) eanNewValue;
        if (StringUtils.isEmpty(ean) || ObjectUtils.equals(eanOldValue, ean)) {
            return true;
        }

        if (productWithEanAlreadyExists(productDD, ean)) {
            product.addError(eanFieldDefinition, "qcadooView.validate.field.error.duplicated");
            return false;
        }

        return true;
    }

    private boolean productWithEanAlreadyExists(final DataDefinition productDD, final String notEmptyEan) {
        SearchCriteriaBuilder scb = productDD.find();
        scb.setProjection(SearchProjections.id());
        scb.add(SearchRestrictions.eq(ProductFields.EAN, notEmptyEan));
        return scb.setMaxResults(1).uniqueResult() != null;
    }

}
