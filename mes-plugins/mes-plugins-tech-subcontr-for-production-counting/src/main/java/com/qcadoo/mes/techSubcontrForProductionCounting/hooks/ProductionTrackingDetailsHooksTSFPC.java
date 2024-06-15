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
package com.qcadoo.mes.techSubcontrForProductionCounting.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.techSubcontrForProductionCounting.constants.ProductionTrackingFieldsTSFPC;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductionTrackingDetailsHooksTSFPC {

    

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void disabledSubcontractorFieldForState(final ViewDefinitionState view) {
        FormComponent productionTrackingForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent subcontractorLookup = (LookupComponent) view
                .getComponentByReference(ProductionTrackingFieldsTSFPC.SUBCONTRACTOR);

        if (productionTrackingForm.getEntityId() == null) {
            return;
        }

        Entity productionTracking = getProductionTrackingFromDB(productionTrackingForm.getEntityId());
        String state = productionTracking.getStringField(ProductionTrackingFields.STATE);

        boolean isDraft = ProductionTrackingStateStringValues.DRAFT.equals(state);
        boolean isExternalSynchronized = productionTracking.getBooleanField(ProductionTrackingFields.IS_EXTERNAL_SYNCHRONIZED);

        subcontractorLookup.setEnabled(isDraft && isExternalSynchronized);
    }

    private Entity getProductionTrackingFromDB(final Long productionTrackingId) {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_TRACKING).get(productionTrackingId);
    }

}
