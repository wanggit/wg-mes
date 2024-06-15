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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.SizeGroupFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class SizeGroupCriteriaModifiers {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void filterSizes(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        Long sizeGroupId = filterValue.getLong("sizeGroupId");

        Entity sizeGroup = getSizeGroupDD().find().add(SearchRestrictions.idEq(sizeGroupId)).setMaxResults(1).uniqueResult();

        List<Entity> sizes = sizeGroup.getManyToManyField(SizeGroupFields.SIZES);

        for (Entity size : sizes) {
            scb.add(SearchRestrictions.idNe(size.getId()));
        }
    }

    private DataDefinition getSizeGroupDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SIZE_GROUP);
    }

}