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
package com.qcadoo.mes.lineChangeoverNorms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ChangeoverNormsServiceImpl implements ChangeoverNormsService {

    @Autowired
    private ChangeoverNormsSearchService changeoverNormsSearchService;

    @Override
    public Entity getMatchingChangeoverNorms(final Entity fromTechnology, final Entity toTechnology, final Entity productionLine) {
        Preconditions.checkArgument(fromTechnology != null, "fromTechnology must be not null.");
        Preconditions.checkArgument(toTechnology != null, "toTechnology must be not null.");
        return changeoverNormsSearchService.findBestMatching(fromTechnology.getId(), extractTechnologyGroupId(fromTechnology),
                toTechnology.getId(), extractTechnologyGroupId(toTechnology), getIdOrNull(productionLine));
    }

    private Long extractTechnologyGroupId(final Entity technology) {
        Entity technologyGroup = technology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP);
        return getIdOrNull(technologyGroup);
    }

    private Long getIdOrNull(final Entity entity) {
        if (entity == null) {
            return null;
        }
        return entity.getId();
    }
}
