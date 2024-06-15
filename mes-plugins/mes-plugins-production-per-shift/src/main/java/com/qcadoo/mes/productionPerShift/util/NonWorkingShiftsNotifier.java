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
package com.qcadoo.mes.productionPerShift.util;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.ShiftFields;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressType;
import com.qcadoo.mes.productionPerShift.dataProvider.ProgressForDayDataProvider;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.qcadoo.view.api.ComponentState.MessageType;

@Service
public class NonWorkingShiftsNotifier {

    private static final Predicate<ShiftAndDate> IS_WORKING_AT_WEEKDAY = new Predicate<ShiftAndDate>() {

        @Override
        public boolean apply(final ShiftAndDate shiftAndDate) {
            return shiftAndDate.isWorking();
        }
    };

    private static final Predicate<ShiftAndDate> SHIFT_STARTED_DAY_BEFORE_ORDER_STARTS = new Predicate<ShiftAndDate>() {

        @Override
        public boolean apply(final ShiftAndDate shiftAndDate) {
            return shiftAndDate.day == 0;
        }
    };

    @Autowired
    private ProgressForDayDataProvider progressForDayDataProvider;

    public void checkAndNotify(final ViewDefinitionState view, final DateTime orderStartDateTime,
            final Entity pps, final ProgressType progressType) {
        for (FormComponent form : view.<FormComponent> tryFindComponentByReference(QcadooViewConstants.L_FORM).asSet()) {
            List<ShiftAndDate> shiftsAndDates = getShiftAndDates(pps, progressType);
            for (ShiftAndDate shiftAndDate : filterShiftsNotStartingOrderAtZeroDay(orderStartDateTime, shiftsAndDates)) {
                notifyAboutShiftNotStartingOrderAtZeroDay(form, shiftAndDate, orderStartDateTime);
            }
            for (ShiftAndDate shiftAndDate : filterShiftsNotWorkingAtWeekday(shiftsAndDates)) {
                notifyAboutShiftNotWorkingAtWeekday(form, shiftAndDate);
            }
        }
    }

    private Iterable<ShiftAndDate> filterShiftsNotStartingOrderAtZeroDay(final DateTime orderStartDateTime,
            final Iterable<ShiftAndDate> shiftsAndDates) {
        return FluentIterable.from(shiftsAndDates).filter(SHIFT_STARTED_DAY_BEFORE_ORDER_STARTS)
                .filter(new Predicate<ShiftAndDate>() {

                    @Override
                    public boolean apply(final ShiftAndDate shiftAndDate) {
                        return !shiftAndDate.isWorking(orderStartDateTime);
                    }
                });
    }

    private Iterable<ShiftAndDate> filterShiftsNotWorkingAtWeekday(final Iterable<ShiftAndDate> shiftsAndDates) {
        return Iterables.filter(shiftsAndDates, Predicates.not(IS_WORKING_AT_WEEKDAY));
    }

    private List<ShiftAndDate> getShiftAndDates(final Entity pps, final ProgressType progressType) {
        List<Entity> allProgresses = progressForDayDataProvider.findForPps(pps, progressType);
        return FluentIterable.from(allProgresses).transformAndConcat(new Function<Entity, Iterable<ShiftAndDate>>() {

            @Override
            public Iterable<ShiftAndDate> apply(final Entity progressForDay) {
                return extractShiftsAndDates(progressForDay);
            }
        }).toList();
    }

    private Iterable<ShiftAndDate> extractShiftsAndDates(final Entity progressForDay) {
        Optional<Date> maybeActualDate = Optional.fromNullable(progressForDay
                .getDateField(ProgressForDayFields.ACTUAL_DATE_OF_DAY));
        for (Date actualDate : maybeActualDate.asSet()) {
            final LocalDate actualLocalDate = LocalDate.fromDateFields(actualDate);
            final int realizationDayNumber = progressForDay.getIntegerField(ProgressForDayFields.DAY);
            return FluentIterable.from(progressForDay.getHasManyField(ProgressForDayFields.DAILY_PROGRESS))
                    .transformAndConcat(new Function<Entity, Iterable<ShiftAndDate>>() {

                        @Override
                        public Iterable<ShiftAndDate> apply(final Entity dailyProgress) {
                            return toShiftAndDates(dailyProgress, realizationDayNumber, actualLocalDate);
                        }
                    }).toList();
        }
        return Collections.emptyList();
    }

    private Iterable<ShiftAndDate> toShiftAndDates(final Entity dailyProgress, final int realizationDayNumber,
            final LocalDate actualLocalDate) {
        return Optional.fromNullable(dailyProgress.getBelongsToField(DailyProgressFields.SHIFT))
                .transform(new Function<Entity, ShiftAndDate>() {

                    @Override
                    public ShiftAndDate apply(final Entity shiftEntity) {
                        Shift shift = new Shift(shiftEntity);
                        return new ShiftAndDate(shift, realizationDayNumber, actualLocalDate);
                    }
                }).asSet();
    }

    private void notifyAboutShiftNotWorkingAtWeekday(final FormComponent form, final ShiftAndDate shiftAndDate) {
        showNotification(form, "productionPerShift.progressForDay.shiftDoesNotWork", shiftAndDate);
    }

    private void notifyAboutShiftNotStartingOrderAtZeroDay(final FormComponent form, final ShiftAndDate shiftAndDate,
            final DateTime dateTime) {
        showNotification(form, "productionPerShift.progressForDay.shiftDoesNotStartOrderAtZeroDay", shiftAndDate);
    }

    private void showNotification(final FormComponent form, final String translationKey, final ShiftAndDate shiftAndDate) {
        Shift shift = shiftAndDate.shift;
        String workDate = DateUtils.toDateString(shiftAndDate.date.toDate());
        String shiftName = shift.getEntity().getStringField(ShiftFields.NAME);
        form.addMessage(translationKey, MessageType.INFO, shiftName, workDate);
    }

    private static class ShiftAndDate {

        private final Shift shift;

        private final int day;

        private final LocalDate date;

        private ShiftAndDate(final Shift shift, final int day, final LocalDate date) {
            this.shift = shift;
            this.day = day;
            this.date = date;
        }

        public boolean isWorking() {
            return shift.worksAt(date);
        }

        public boolean isWorking(final DateTime dateTime) {
            return shift.worksAt(dateTime);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            ShiftAndDate rhs = (ShiftAndDate) obj;
            return new EqualsBuilder().append(this.shift, rhs.shift).append(this.day, rhs.day).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(shift).append(day).append(date).toHashCode();
        }
    }

}
