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

import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.PREVIOUS_ORDER;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class OrderHooksLCNFOTest {

    private OrderHooksLCNFO orderHooksLCNFO;

    @Mock
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    @Mock
    private DataDefinition orderDD;

    @Mock
    private Entity order, previousOrderDB, orderDB;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderHooksLCNFO = new OrderHooksLCNFO();

        setField(orderHooksLCNFO, "lineChangeoverNormsForOrdersService", lineChangeoverNormsForOrdersService);

        stubBelongsToField(order, OrderFieldsLCNFO.PREVIOUS_ORDER, previousOrderDB);
        stubBelongsToField(order, OrderFieldsLCNFO.ORDER, orderDB);
    }

    @Test
    public void shouldReturnFalseWhenPreviousOrderIsNotCorrect() {
        // given
        given(lineChangeoverNormsForOrdersService.previousOrderEndsBeforeOrIsWithdrawed(previousOrderDB, orderDB)).willReturn(
                false);

        // when
        boolean result = orderHooksLCNFO.checkIfOrderHasCorrectStateAndIsPrevious(orderDD, order);

        // then
        assertFalse(result);

        verify(order).addError(Mockito.eq(orderDD.getField(PREVIOUS_ORDER)), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenPreviousOrderIsCorrect() {
        // given
        given(lineChangeoverNormsForOrdersService.previousOrderEndsBeforeOrIsWithdrawed(previousOrderDB, orderDB)).willReturn(
                true);

        // when
        boolean result = orderHooksLCNFO.checkIfOrderHasCorrectStateAndIsPrevious(orderDD, order);

        // then
        assertTrue(result);

        verify(order, never()).addError(Mockito.eq(orderDD.getField(PREVIOUS_ORDER)), Mockito.anyString());
    }
}
