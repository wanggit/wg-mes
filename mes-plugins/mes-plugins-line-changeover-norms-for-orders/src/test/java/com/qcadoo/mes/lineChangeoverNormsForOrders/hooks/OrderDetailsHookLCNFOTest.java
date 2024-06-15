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
package com.qcadoo.mes.lineChangeoverNormsForOrders.hooks;

import com.google.common.base.Predicate;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.testing.model.EntityTestUtils;
import com.qcadoo.view.api.ViewDefinitionState;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class OrderDetailsHookLCNFOTest {

    private OrderDetailsHooksLCNFO orderDetailsHooksLCNFO;

    @Mock
    private OrderDetailsRibbonHelper orderDetailsRibbonHelper;

    @Mock
    private Entity order, productionLine, technology;

    @Mock
    private ViewDefinitionState view;

    @Captor
    private ArgumentCaptor<Predicate<Entity>> predicateCaptor;

    @Before
    public void init() {
        orderDetailsHooksLCNFO = new OrderDetailsHooksLCNFO();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(orderDetailsHooksLCNFO, "orderDetailsRibbonHelper", orderDetailsRibbonHelper);
    }

    private void stubTechnology(final Entity technology, final Entity productionLine) {
        EntityTestUtils.stubBelongsToField(order, OrderFields.TECHNOLOGY, technology);
        EntityTestUtils.stubBelongsToField(order, OrderFields.PRODUCTION_LINE, productionLine);

    }

    @Ignore
    @Test
    public final void shouldDelegateToOrderDetailsRibbonHelperAndUseValidPredicate() {
        // when
        orderDetailsHooksLCNFO.onBeforeRender(view);

        // then
        verify(orderDetailsRibbonHelper).setButtonEnabled(any(ViewDefinitionState.class), eq("changeover"), eq("showChangeover"),
                predicateCaptor.capture()::apply, Optional.of("test"));
        Predicate<Entity> predicate = predicateCaptor.getValue();

        assertFalse(predicate.apply(null));

        stubTechnology(null, null);
        assertFalse(predicate.apply(order));

        stubTechnology(technology, productionLine);
        assertTrue(predicate.apply(order));
    }
}
