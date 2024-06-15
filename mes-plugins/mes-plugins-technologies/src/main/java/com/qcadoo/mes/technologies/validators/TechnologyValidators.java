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
package com.qcadoo.mes.technologies.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ProductService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyValidators {

    @Autowired
    private ProductService productService;

    public boolean validatesWith(final DataDefinition technologyDD, final Entity technology) {
        boolean isValid = checkTechnologyDefault(technologyDD, technology);
        isValid = isValid && productService.checkIfProductIsNotRemoved(technologyDD, technology);

        return isValid;
    }

    public boolean checkTechnologyDefault(final DataDefinition technologyDD, final Entity technology) {
        if (!technology.getBooleanField(TechnologyFields.MASTER)) {
            return true;
        }

        if (!hasInCorrectStateTechnologyForMaster(technology)) {
            technology.addError(technologyDD.getField(TechnologyFields.MASTER),
                    "technologies.technology.validate.global.error.default.incorrectState");

            return false;
        }

        return true;
    }

    private boolean hasInCorrectStateTechnologyForMaster(final Entity technology) {
        return technology.getStringField(TechnologyFields.STATE).equals(TechnologyStateStringValues.ACCEPTED)
                || technology.getStringField(TechnologyFields.STATE).equals(TechnologyStateStringValues.OUTDATED);
    }

}
