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
package com.qcadoo.mes.materialFlow.listeners;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Component
public class ParametersListenersMF {

    public void redirectToDashboardParameters(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long parameterId = (Long) state.getFieldValue();

        if (Objects.nonNull(parameterId)) {
            String url = "../page/materialFlow/dashboardParameters.html?context={\"form.id\":\"" + parameterId + "\"}";
            view.redirectTo(url, false, true);
        }
    }

}
