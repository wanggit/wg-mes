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
package com.qcadoo.mes.productionCounting.listeners;

import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingForProductDtoFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductionTrackingsForProductListListeners {



    public void goToProductionTracking(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        if (!grid.getSelectedEntitiesIds().isEmpty()) {
            String url = "../page/productionCounting/productionTrackingDetails.html";

            Entity productionTrackingForProductDto = grid.getSelectedEntities().get(0);

            view.redirectTo(url, false, true,
                    ImmutableMap.of("form.id", productionTrackingForProductDto
                            .getIntegerField(ProductionTrackingForProductDtoFields.PRODUCTION_TRACKING_ID)));
        }
    }

}
