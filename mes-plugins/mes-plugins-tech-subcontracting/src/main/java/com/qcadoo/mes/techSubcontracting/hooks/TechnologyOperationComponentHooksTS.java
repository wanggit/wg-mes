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
package com.qcadoo.mes.techSubcontracting.hooks;

import com.qcadoo.mes.techSubcontracting.constants.OperationFieldsTS;
import com.qcadoo.mes.techSubcontracting.constants.TechnologyOperationComponentFieldsTS;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TechnologyOperationComponentHooksTS {

    public void copySubcontractingFieldsFromLowerInstance(final DataDefinition technologyOperationComponentDD,
                                                          final Entity technologyOperationComponent) {
        if (shouldCopyFromLowerInstance(technologyOperationComponent)) {
            Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

            if (Objects.nonNull(operation)) {
                technologyOperationComponent.setField(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING, operation.getBooleanField(OperationFieldsTS.IS_SUBCONTRACTING));
                technologyOperationComponent.setField(TechnologyOperationComponentFieldsTS.UNIT_COST, operation.getDecimalField(OperationFieldsTS.UNIT_COST));
            }
        }
    }

    private boolean shouldCopyFromLowerInstance(final Entity technologyOperationComponent) {
        return Objects.isNull(technologyOperationComponent.getField(TechnologyOperationComponentFieldsTS.IS_SUBCONTRACTING));
    }

}
