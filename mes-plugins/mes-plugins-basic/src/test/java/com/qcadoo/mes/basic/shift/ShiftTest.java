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
package com.qcadoo.mes.basic.shift;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.mes.basic.constants.ShiftFields;
import com.qcadoo.mes.basic.constants.ShiftTimetableExceptionFields;
import com.qcadoo.mes.basic.constants.TimetableExceptionType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ShiftTest {

    @Mock
    private Entity shiftEntity;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        given(shiftEntity.copy()).willReturn(shiftEntity);
        EntityList emptyEntityList = mockEntityList(Lists.<Entity> newArrayList());
        given(shiftEntity.getHasManyField(Mockito.anyString())).willReturn(emptyEntityList);
    }

    @Test
    public final void shouldParseTimeTableExceptions() {
        // given
        String hours = "6:00-12:00, 21:30-2:30";
        given(shiftEntity.getStringField(ShiftFields.MONDAY_HOURS)).willReturn(hours);
        given(shiftEntity.getBooleanField(ShiftFields.MONDAY_WORKING)).willReturn(true);

        DateTime mondayMidnight = new DateTime(2013, 9, 2, 0, 0);

        DateTime freeTimeBegin = mondayMidnight.plusHours(8);
        DateTime freeTimeEnd = freeTimeBegin.plusHours(4);
        Entity freeTime = mockTimetableException(TimetableExceptionType.FREE_TIME, freeTimeBegin, freeTimeEnd);

        DateTime workTimeBegin = mondayMidnight.plusHours(20);
        DateTime workTimeEnd = workTimeBegin.plusHours(2);
        Entity workTime = mockTimetableException(TimetableExceptionType.WORK_TIME, workTimeBegin, workTimeEnd);

        EntityList timetableExceptionsList = mockEntityList(Lists.newArrayList(freeTime, workTime));
        given(shiftEntity.getHasManyField(ShiftFields.TIMETABLE_EXCEPTIONS)).willReturn(timetableExceptionsList);

        // when
        Shift shift = new Shift(shiftEntity);

        // then
        assertTrue(shift.worksAt(DateTimeConstants.MONDAY, new LocalTime(10, 30)));
        assertFalse(shift.worksAt(DateTimeConstants.SUNDAY, new LocalTime(12, 0)));
        assertFalse(shift.worksAt(DateTimeConstants.MONDAY, new LocalTime(12, 01)));

        assertEquals(Optional.of(new DateRange(mondayMidnight.plusHours(6).toDate(), mondayMidnight.plusHours(12).toDate())),
                shift.findWorkTimeAt(mondayMidnight.plusHours(7).toDate()));
        assertFalse(shift.findWorkTimeAt(mondayMidnight.plusDays(2).toDate()).isPresent());
        assertFalse(shift.findWorkTimeAt(mondayMidnight.plusHours(9).toDate()).isPresent());
    }

    @Test
    public final void shouldReturnWorkingHoursForDay() {
        // given
        String hours = "6:00-12:00, 21:30-2:30";
        given(shiftEntity.getStringField(ShiftFields.MONDAY_HOURS)).willReturn(hours);
        given(shiftEntity.getBooleanField(ShiftFields.MONDAY_WORKING)).willReturn(true);

        LocalDate mondayDate = new LocalDate(2013, 9, 2);

        // when
        Shift shift = new Shift(shiftEntity);
        List<TimeRange> timeRanges = shift.findWorkTimeAt(mondayDate);

        // then
        List<TimeRange> expectedTimeRanges = ImmutableList.of(new TimeRange(new LocalTime(6, 0), new LocalTime(12, 0)),
                new TimeRange(new LocalTime(21, 30), new LocalTime(2, 30)));
        assertEquals(expectedTimeRanges, timeRanges);
    }

    private EntityList mockEntityList(final List<Entity> entities) {
        EntityList entityList = mock(EntityList.class);
        given(entityList.iterator()).willReturn(entities.iterator());
        given(entityList.isEmpty()).willReturn(entities.isEmpty());
        return entityList;
    }

    private Entity mockTimetableException(final TimetableExceptionType type, final DateTime from, final DateTime to) {
        Entity timetableException = mock(Entity.class);
        given(timetableException.getStringField(ShiftTimetableExceptionFields.TYPE)).willReturn(type.getStringValue());
        given(timetableException.getDateField(ShiftTimetableExceptionFields.FROM_DATE)).willReturn(from.toDate());
        given(timetableException.getDateField(ShiftTimetableExceptionFields.TO_DATE)).willReturn(to.toDate());
        return timetableException;
    }

}
