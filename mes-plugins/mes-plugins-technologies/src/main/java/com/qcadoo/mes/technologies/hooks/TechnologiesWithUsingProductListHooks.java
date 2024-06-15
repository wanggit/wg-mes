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
package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class TechnologiesWithUsingProductListHooks {



    

    public void beforeRender(final ViewDefinitionState viewDefinitionState) {
        showInfoIfNotUsed(viewDefinitionState);
    }

    private void showInfoIfNotUsed(final ViewDefinitionState viewDefinitionState) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference(QcadooViewConstants.L_GRID);
        if (grid.getEntities().isEmpty()) {
            ComponentState form = viewDefinitionState.getComponentByReference(QcadooViewConstants.L_FORM);
            form.addMessage("technologies.product.info.notUsed", MessageType.INFO, true);
        }
    }

}
