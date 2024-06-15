/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.basic.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ModelFields;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ModelCriteriaModifiers {

    public static final String L_ASSORTMENT_ID = "assortmentId";

    public void showModelsWithoutAssortment(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNull(ModelFields.ASSORTMENT));
    }

    public void showModelWithAssortment(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(L_ASSORTMENT_ID)) {
            Long assortmentId = filterValueHolder.getLong(L_ASSORTMENT_ID);

            scb.createAlias(ModelFields.ASSORTMENT, ModelFields.ASSORTMENT, JoinType.LEFT);
            scb.add(SearchRestrictions.eq(ModelFields.ASSORTMENT + ".id", assortmentId));
        }
    }

}