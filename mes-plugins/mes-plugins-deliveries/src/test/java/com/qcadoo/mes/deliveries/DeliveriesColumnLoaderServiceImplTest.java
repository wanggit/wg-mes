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
package com.qcadoo.mes.deliveries;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.columnExtension.ColumnExtensionService;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;

public class DeliveriesColumnLoaderServiceImplTest {

    private DeliveriesColumnLoaderService deliveriesColumnLoaderService;

    @Mock
    private ColumnExtensionService columnExtensionService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveriesColumnLoaderService = new DeliveriesColumnLoaderServiceImpl();

        ReflectionTestUtils.setField(deliveriesColumnLoaderService, "columnExtensionService", columnExtensionService);
    }

    @Test
    public void shouldReturnTrueWhenIsColumnsForDeliveriesEmpty() {
        // given
        given(
                columnExtensionService.isColumnsEmpty(DeliveriesConstants.PLUGIN_IDENTIFIER,
                        DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES)).willReturn(true);

        // when
        boolean result = deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenIsColumnsForDeliveriesEmpty() {
        // given
        given(
                columnExtensionService.isColumnsEmpty(DeliveriesConstants.PLUGIN_IDENTIFIER,
                        DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES)).willReturn(false);

        // when
        boolean result = deliveriesColumnLoaderService.isColumnsForDeliveriesEmpty();

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenIsColumnsForOrdersEmpty() {
        // given
        given(
                columnExtensionService.isColumnsEmpty(DeliveriesConstants.PLUGIN_IDENTIFIER,
                        DeliveriesConstants.MODEL_COLUMN_FOR_ORDERS)).willReturn(true);

        // when
        boolean result = deliveriesColumnLoaderService.isColumnsForOrdersEmpty();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenIsColumnsForOrdersEmpty() {
        // given
        given(
                columnExtensionService.isColumnsEmpty(DeliveriesConstants.PLUGIN_IDENTIFIER,
                        DeliveriesConstants.MODEL_COLUMN_FOR_ORDERS)).willReturn(false);

        // when
        boolean result = deliveriesColumnLoaderService.isColumnsForOrdersEmpty();

        // then
        assertFalse(result);
    }

}
