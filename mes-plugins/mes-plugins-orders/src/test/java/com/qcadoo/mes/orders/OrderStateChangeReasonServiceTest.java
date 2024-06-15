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
package com.qcadoo.mes.orders;

import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DELAYED_EFFECTIVE_DATE_FROM_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DELAYED_EFFECTIVE_DATE_TO_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.EARLIER_EFFECTIVE_DATE_FROM_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.EARLIER_EFFECTIVE_DATE_TO_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_CHANGING_STATE_TO_ABANDONED;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_CHANGING_STATE_TO_DECLINED;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_CHANGING_STATE_TO_INTERRUPTED;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_DATE_TO;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.model.api.Entity;

public class OrderStateChangeReasonServiceTest {

    private OrderStateChangeReasonService orderStateChangeReasonService;

    @Mock
    private ParameterService parameterService;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        orderStateChangeReasonService = new OrderStateChangeReasonService();
        setField(orderStateChangeReasonService, "parameterService", parameterService);
    }

    @Test
    public void shouldReturnTrueIfIsReasonNeededWhenCorrectingDateFrom() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_CORRECTING_DATE_FROM)).willReturn(true);

        // when
        boolean result = orderStateChangeReasonService.neededWhenCorrectingDateFrom();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseIfIsReasonNeededWhenCorrectingDateFrom() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_CORRECTING_DATE_FROM)).willReturn(false);

        // when
        boolean result = orderStateChangeReasonService.neededWhenCorrectingDateFrom();

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueIfIsReasonNeededWhenCorrectingDateTo() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_CORRECTING_DATE_TO)).willReturn(true);

        // when
        boolean result = orderStateChangeReasonService.neededWhenCorrectingDateTo();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseIfIsReasonNeededWhenCorrectingDateTo() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_CORRECTING_DATE_TO)).willReturn(false);

        // when
        boolean result = orderStateChangeReasonService.neededWhenCorrectingDateTo();

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueIfIsReasonNeededWhenChangingStateToDeclined() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_CHANGING_STATE_TO_DECLINED)).willReturn(true);

        // when
        boolean result = orderStateChangeReasonService.neededForDecline();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseIfIsReasonNeededWhenChangingStateToDeclined() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_CHANGING_STATE_TO_DECLINED)).willReturn(false);

        // when
        boolean result = orderStateChangeReasonService.neededForDecline();

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueIfIsReasonNeededWhenChangingStateToInterrupted() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_CHANGING_STATE_TO_INTERRUPTED)).willReturn(true);

        // when
        boolean result = orderStateChangeReasonService.neededForInterrupt();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseIfIsReasonNeededWhenChangingStateToInterrupted() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_CHANGING_STATE_TO_INTERRUPTED)).willReturn(false);

        // when
        boolean result = orderStateChangeReasonService.neededForInterrupt();

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueIfIsReasonNeededWhenChangingStateToAbandoned() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_CHANGING_STATE_TO_ABANDONED)).willReturn(true);

        // when
        boolean result = orderStateChangeReasonService.neededForAbandon();

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseIfIsReasonNeededWhenChangingStateToAbandoned() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_CHANGING_STATE_TO_ABANDONED)).willReturn(false);

        // when
        boolean result = orderStateChangeReasonService.neededForAbandon();

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnNonZeroIfIsReasonNeededWhenChangingEffectiveDateFrom() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        Entity order = mock(Entity.class);

        Date dateFrom = mock(Date.class);
        Date correctedDateFrom = null;
        Date effectiveDateFrom = mock(Date.class);

        long dateFromTime = 10000L;
        long effectiveDateFromTime = 15000L;

        Integer delayedDateFromTime = 2;
        Integer earlierDateFromTime = 2;

        given(order.getField(DATE_FROM)).willReturn(dateFrom);
        given(order.getField(CORRECTED_DATE_FROM)).willReturn(correctedDateFrom);
        given(order.getField(EFFECTIVE_DATE_FROM)).willReturn(effectiveDateFrom);

        given(dateFrom.getTime()).willReturn(dateFromTime);
        given(effectiveDateFrom.getTime()).willReturn(effectiveDateFromTime);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM)).willReturn(true);
        given(parameter.getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM)).willReturn(true);

        given(parameter.getField(DELAYED_EFFECTIVE_DATE_FROM_TIME)).willReturn(delayedDateFromTime);
        given(parameter.getField(EARLIER_EFFECTIVE_DATE_FROM_TIME)).willReturn(earlierDateFromTime);

        // when
        long result = orderStateChangeReasonService.getEffectiveDateFromDifference(parameter, order);

        // then
        assertEquals(5L, result);
    }

    @Test
    public void shouldReturnNonZeroIfIsReasonNeededWhenChangingEffectiveDateFromAndCorrectedDateFromIsntNull() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        Entity order = mock(Entity.class);

        Date dateFrom = mock(Date.class);
        Date correctedDateFrom = mock(Date.class);
        Date effectiveDateFrom = mock(Date.class);

        long dateFromTime = 10000L;
        long effectiveDateFromTime = 15000L;

        Integer delayedDateFromTime = 2;
        Integer earlierDateFromTime = 2;

        given(order.getField(DATE_FROM)).willReturn(dateFrom);
        given(order.getField(CORRECTED_DATE_FROM)).willReturn(correctedDateFrom);
        given(order.getField(EFFECTIVE_DATE_FROM)).willReturn(effectiveDateFrom);

        given(correctedDateFrom.getTime()).willReturn(dateFromTime);
        given(effectiveDateFrom.getTime()).willReturn(effectiveDateFromTime);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM)).willReturn(true);
        given(parameter.getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM)).willReturn(true);

        given(parameter.getField(DELAYED_EFFECTIVE_DATE_FROM_TIME)).willReturn(delayedDateFromTime);
        given(parameter.getField(EARLIER_EFFECTIVE_DATE_FROM_TIME)).willReturn(earlierDateFromTime);

        // when
        long result = orderStateChangeReasonService.getEffectiveDateFromDifference(parameter, order);

        // then
        assertEquals(5L, result);
    }

    @Test
    public void shouldReturnZeroIfIsReasonNeededWhenChangingEffectiveDateFromAndDateFromAndEffectiveDateFromIsNull() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        Entity order = mock(Entity.class);

        given(order.getField(DATE_FROM)).willReturn(null);
        given(order.getField(CORRECTED_DATE_FROM)).willReturn(null);
        given(order.getField(EFFECTIVE_DATE_FROM)).willReturn(null);

        // when
        long result = orderStateChangeReasonService.getEffectiveDateFromDifference(parameter, order);

        // then
        assertTrue(result == 0);
    }

    @Test
    public void shouldReturnZeroIfIsReasonNeededWhenChangingEffectiveDateFromAndReasonsAreFalse() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        Entity order = mock(Entity.class);

        Date dateFrom = mock(Date.class);
        Date correctedDateFrom = null;
        Date effectiveDateFrom = mock(Date.class);

        long dateFromTime = 10000L;
        long effectiveDateFromTime = 15000L;

        Integer delayedDateFromTime = 2;
        Integer earlierDateFromTime = 2;

        given(order.getField(DATE_FROM)).willReturn(dateFrom);
        given(order.getField(CORRECTED_DATE_FROM)).willReturn(correctedDateFrom);
        given(order.getField(EFFECTIVE_DATE_FROM)).willReturn(effectiveDateFrom);

        given(dateFrom.getTime()).willReturn(dateFromTime);
        given(effectiveDateFrom.getTime()).willReturn(effectiveDateFromTime);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM)).willReturn(false);
        given(parameter.getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM)).willReturn(false);

        given(parameter.getField(DELAYED_EFFECTIVE_DATE_FROM_TIME)).willReturn(delayedDateFromTime);
        given(parameter.getField(EARLIER_EFFECTIVE_DATE_FROM_TIME)).willReturn(earlierDateFromTime);

        // when
        long result = orderStateChangeReasonService.getEffectiveDateFromDifference(parameter, order);

        // then
        assertTrue(result == 0L);
    }

    @Test
    public void shouldReturnNonZeroIfIsReasonNeededWhenChangingEffectiveDateTo() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        Entity order = mock(Entity.class);

        Date dateTo = mock(Date.class);
        Date correctedDateTo = null;
        Date effectiveDateTo = mock(Date.class);

        long dateToTime = 10000L;
        long effectiveDateToTime = 15000L;

        Integer delayedDateToTime = 2;
        Integer earlierDateToTime = 2;

        given(order.getField(DATE_TO)).willReturn(dateTo);
        given(order.getField(CORRECTED_DATE_TO)).willReturn(correctedDateTo);
        given(order.getField(EFFECTIVE_DATE_TO)).willReturn(effectiveDateTo);

        given(dateTo.getTime()).willReturn(dateToTime);
        given(effectiveDateTo.getTime()).willReturn(effectiveDateToTime);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO)).willReturn(true);
        given(parameter.getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO)).willReturn(true);

        given(parameter.getField(DELAYED_EFFECTIVE_DATE_TO_TIME)).willReturn(delayedDateToTime);
        given(parameter.getField(EARLIER_EFFECTIVE_DATE_TO_TIME)).willReturn(earlierDateToTime);

        // when
        long result = orderStateChangeReasonService.getEffectiveDateToDifference(parameter, order);

        // then
        assertEquals(5L, result);
    }

    @Test
    public void shouldReturnNonZeroIfIsReasonNeededWhenChangingEffectiveDateToAndCorrectedDateToIsntNull() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        Entity order = mock(Entity.class);

        Date dateTo = mock(Date.class);
        Date correctedDateTo = mock(Date.class);
        Date effectiveDateTo = mock(Date.class);

        long dateToTime = 10000L;
        long effectiveDateToTime = 15000L;

        Integer delayedDateToTime = 2;
        Integer earlierDateToTime = 2;

        given(order.getField(DATE_TO)).willReturn(dateTo);
        given(order.getField(CORRECTED_DATE_TO)).willReturn(correctedDateTo);
        given(order.getField(EFFECTIVE_DATE_TO)).willReturn(effectiveDateTo);

        given(correctedDateTo.getTime()).willReturn(dateToTime);
        given(effectiveDateTo.getTime()).willReturn(effectiveDateToTime);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO)).willReturn(true);
        given(parameter.getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO)).willReturn(true);

        given(parameter.getField(DELAYED_EFFECTIVE_DATE_TO_TIME)).willReturn(delayedDateToTime);
        given(parameter.getField(EARLIER_EFFECTIVE_DATE_TO_TIME)).willReturn(earlierDateToTime);

        // when
        long result = orderStateChangeReasonService.getEffectiveDateToDifference(parameter, order);

        // then
        assertEquals(5L, result);
    }

    @Test
    public void shouldReturnZeroIfIsReasonNeededWhenChangingEffectiveDateToAndDateToAndEffectiveDateToIsNull() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        Entity order = mock(Entity.class);

        given(order.getField(DATE_TO)).willReturn(null);
        given(order.getField(CORRECTED_DATE_TO)).willReturn(null);
        given(order.getField(EFFECTIVE_DATE_TO)).willReturn(null);

        // when
        long result = orderStateChangeReasonService.getEffectiveDateToDifference(parameter, order);

        // then
        assertTrue(result == 0L);
    }

    @Test
    public void shouldReturnZeroIfIsReasonNeededWhenChangingEffectiveDateToAndReasonsAreFalse() {
        // given
        Entity parameter = mock(Entity.class);

        given(parameterService.getParameter()).willReturn(parameter);

        Entity order = mock(Entity.class);

        Date dateTo = mock(Date.class);
        Date correctedDateTo = null;
        Date effectiveDateTo = mock(Date.class);

        long dateToTime = 10000L;
        long effectiveDateToTime = 15000L;

        Integer delayedDateToTime = 2;
        Integer earlierDateToTime = 2;

        given(order.getField(DATE_TO)).willReturn(dateTo);
        given(order.getField(CORRECTED_DATE_TO)).willReturn(correctedDateTo);
        given(order.getField(EFFECTIVE_DATE_TO)).willReturn(effectiveDateTo);

        given(dateTo.getTime()).willReturn(dateToTime);
        given(effectiveDateTo.getTime()).willReturn(effectiveDateToTime);

        given(parameter.getBooleanField(REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO)).willReturn(false);
        given(parameter.getBooleanField(REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO)).willReturn(false);

        given(parameter.getField(DELAYED_EFFECTIVE_DATE_TO_TIME)).willReturn(delayedDateToTime);
        given(parameter.getField(EARLIER_EFFECTIVE_DATE_TO_TIME)).willReturn(earlierDateToTime);

        // when
        long result = orderStateChangeReasonService.getEffectiveDateToDifference(parameter, order);

        // then
        assertTrue(result == 0L);
    }
}
