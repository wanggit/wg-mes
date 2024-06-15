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
package com.qcadoo.mes.productionLines.hooks;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.BDDMockito.given;

public class ProductionLineDetailsViewHooksTest {

    

    private ProductionLineDetailsViewHooks productionLinesViewHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent productionLineForm;

    @Mock
    private ComponentState supportsAllTechnologies, groupsGrid, technologiesGrid;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productionLinesViewHooks = new ProductionLineDetailsViewHooks();

        given(view.getComponentByReference(QcadooViewConstants.L_FORM)).willReturn(productionLineForm);
    }

}
