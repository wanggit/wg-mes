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
package com.qcadoo.mes.cmmsMachineParts.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.DocumentFieldsCMP;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service public class DocumentDetailsListenersCMP {

    public void clearEvents(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String args[]) {

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity document = form.getPersistedEntityWithIncludedFormValues();
        String type = document.getStringField(DocumentFields.TYPE);

        if (type.compareTo(DocumentType.INTERNAL_OUTBOUND.getStringValue()) != 0) {
            LookupComponent mEventLookup = (LookupComponent) viewDefinitionState
                    .getComponentByReference(DocumentFieldsCMP.MAINTENANCE_EVENT);
            mEventLookup.setFieldValue(null);
            LookupComponent pEventLookup = (LookupComponent) viewDefinitionState
                    .getComponentByReference(DocumentFieldsCMP.PLANNED_EVENT);
            pEventLookup.setFieldValue(null);
        }
    }
}
