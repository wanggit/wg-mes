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
package com.qcadoo.mes.timeNormsForOperations.states;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class TechnologyStateChangeListenerServiceTNFO {

    @Autowired
    private NormService normService;

    public boolean checkOperationOutputQuantities(final StateChangeContext stateChangeContext) {
        Entity techology = stateChangeContext.getOwner();
        // FIXME DEV_TEAM, why would I need this? Without it operationComponents are null
        Entity technology = techology.getDataDefinition().get(techology.getId());

        List<String> messages = normService.checkOperationOutputQuantities(technology);
        if (!messages.isEmpty()) {
            stateChangeContext.addValidationError("technologies.technology.validate.global.error.treeIsNotValid");
            StringBuilder builder = new StringBuilder();
            for (String message : messages) {
                builder.append(message);
                builder.append(", ");
            }
            stateChangeContext.addMessage("technologies.technology.validate.error.invalidQuantity", StateMessageType.FAILURE,
                    false, builder.toString());
        }
        return messages.isEmpty();
    }

    public boolean checkIfAllOperationComponenthHaveTJSet(final StateChangeContext stateChangeContext) {
        Entity technology = stateChangeContext.getOwner();
        DataDefinition dataDefinition = technology.getDataDefinition();
        StringBuilder errors = new StringBuilder();
        Entity savedTechnology = dataDefinition.get(technology.getId());
        EntityTree operationComponents = savedTechnology.getTreeField("operationComponents");
        boolean isValid = true;
        for (Entity operationComponent : operationComponents) {
            if (!checkIfTJSet(operationComponent)) {
                isValid = false;
                errors.append(" ");
                StringBuilder fieldName = new StringBuilder();
                fieldName.append(operationComponent.getStringField("nodeNumber")).append(" ");
                fieldName.append(operationComponent.getBelongsToField("operation").getStringField("number")).append(" ");
                fieldName.append(operationComponent.getBelongsToField("operation").getStringField("name"));
                errors.append(fieldName);
                errors.append(", ");
            }
        }
        if (!isValid) {
            stateChangeContext.addValidationError("technologies.technology.validate.global.error.treeIsNotValid");
            stateChangeContext.addMessage("technologies.technology.validate.global.error.noTJSpecified",
                    StateMessageType.FAILURE, false, errors.toString());
        }
        return isValid;
    }

    private boolean checkIfTJSet(final Entity operationComponent) {
        return operationComponent.getField("tj") != null;
    }

}
