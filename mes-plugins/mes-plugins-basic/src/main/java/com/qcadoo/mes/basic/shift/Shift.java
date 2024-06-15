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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.commons.functional.BiFunction;
import com.qcadoo.commons.functional.Fold;
import com.qcadoo.mes.basic.constants.TimetableExceptionType;
import com.qcadoo.model.api.Entity;

/**
 * Shift with some common methods for checking its work time.
 * 
 * This class assumes that first day of week is Monday (as opposite to the Calendar constants, where first day of week is Sunday).
 * Be aware of that.
 * 
 * @author Marcin Kubala
 * @since 1.2.1
 */
public class Shift {

    private final Entity shift;

    private final Long shiftId;

    private final Multimap<Integer, WorkingHours> workingHoursPerDay;

    private final ShiftTimetableExceptions timetableExceptions;

    private DateTime shiftStartDate;

    private DateTime shiftEndDate;

    public Shift(final Entity shiftEntity) {
        Entity shiftEntityCopy = shiftEntity.copy();

        this.shift = shiftEntityCopy;
        this.shiftId = shiftEntityCopy.getId();
        this.workingHoursPerDay = getWorkingHoursPerDay(shiftEntityCopy, true);
        this.timetableExceptions = new ShiftTimetableExceptions(shiftEntityCopy);
    }

    public Shift(final Entity shiftEntity, final DateTime day) {
        Entity shiftEntityCopy = shiftEntity.copy();

        this.shift = shiftEntityCopy;
        this.shiftId = shiftEntityCopy.getId();
        this.workingHoursPerDay = getWorkingHoursPerDay(shiftEntityCopy, true);
        this.timetableExceptions = new ShiftTimetableExceptions(shiftEntityCopy);

        getShiftDates(day);
    }

    public Shift(final Entity shiftEntity, final DateTime day, final boolean checkWorking) {
        Entity shiftEntityCopy = shiftEntity.copy();

        this.shift = shiftEntityCopy;
        this.shiftId = shiftEntityCopy.getId();
        this.workingHoursPerDay = getWorkingHoursPerDay(shiftEntityCopy, checkWorking);
        this.timetableExceptions = new ShiftTimetableExceptions(shiftEntityCopy);

        getShiftDates(day);
    }

    private void getShiftDates(DateTime day) {
        DateTime dateTime = day.withTimeAtStartOfDay();

        Optional<TimeRange> orange = findWorkTimeAt(dateTime.getDayOfWeek());

        if (orange.isPresent()) {
            TimeRange range = orange.get();

            shiftStartDate = dateTime;
            shiftStartDate = shiftStartDate.withHourOfDay(range.getFrom().getHourOfDay());
            shiftStartDate = shiftStartDate.withMinuteOfHour(range.getFrom().getMinuteOfHour());
            shiftEndDate = dateTime;
            shiftEndDate = shiftEndDate.withHourOfDay(range.getTo().getHourOfDay());
            shiftEndDate = shiftEndDate.withMinuteOfHour(range.getTo().getMinuteOfHour());

            if (shiftStartDate.isAfter(shiftEndDate)) {
                shiftEndDate = shiftEndDate.plusDays(1);
            }
        } else {
            shiftStartDate = null;
            shiftEndDate = null;
        }
    }

    private Multimap<Integer, WorkingHours> getWorkingHoursPerDay(final Entity shiftEntity, final boolean checkWorking) {
        ImmutableSetMultimap.Builder<Integer, WorkingHours> builder = ImmutableSetMultimap.builder();

        for (Entry<Integer, String> dayNumToName : Constants.DAYS_OF_WEEK.entrySet()) {
            boolean isWorking = checkWorking ? shiftEntity.getBooleanField(dayNumToName.getValue() + "Working") : true;

            if (isWorking) {
                String hoursRanges = shiftEntity.getStringField(dayNumToName.getValue() + "Hours");
                WorkingHours workingHoursForGivenDay = new WorkingHours(hoursRanges);
                builder.put(dayNumToName.getKey(), workingHoursForGivenDay);
            }
        }

        return builder.build();
    }

    /**
     * Check if this shift works at given day of week and time. This method is NOT aware of timetable exceptions.
     * 
     * @param dayOfWeek
     *            1 == MONDAY !
     * @param time
     * @return true if this shift works at given day of week and time.
     */
    public boolean worksAt(final int dayOfWeek, final LocalTime time) {
        return findWorkTimeAt(dayOfWeek, time).isPresent();
    }

    /**
     * Check if this shift works at given day of week. This method is NOT aware of timetable exceptions.
     * 
     * @param dayOfWeek
     *            1 == MONDAY !
     * @return true if this shift works at given day of week.
     */
    public boolean worksAt(final int dayOfWeek) {
        Preconditions.checkArgument(dayOfWeek > 0 && dayOfWeek <= 7, "Given day of week have to be > 0 and <= 7.");
        return shift.getBooleanField(Constants.DAYS_OF_WEEK.get(dayOfWeek) + "Working");
    }

    /**
     * Check if this shift will be working at given local date. This method is NOT aware of timetable exceptions
     * 
     * @param localDate
     * @return true if this shift will be working at given local date.
     */
    public boolean worksAt(final LocalDate localDate) {
        return worksAt(localDate.getDayOfWeek());
    }

    /**
     * Check if this shift will be working at given date and time. This method is aware of timetable exceptions
     * 
     * @param dateTime
     * @return true if this shift will be working at given date and time.
     */
    public boolean worksAt(final DateTime dateTime) {
        return worksAt(dateTime.toDate());
    }

    /**
     * Check if this shift will be working at given date and time. This method is aware of timetable exceptions
     * 
     * @param date
     * @return true if this shift will be working at given date and time.
     * 
     * @deprecated use worksAt(DateTime) if you want to check if shift works at given date and time, or works(LocalDate) if all
     *             you want is just check if shift works at given day.
     */
    @Deprecated
    public boolean worksAt(final Date date) {
        DateTime dateTime = new DateTime(date);
        return (worksAt(dateTime.getDayOfWeek(), dateTime.toLocalTime()) && !timetableExceptions.hasFreeTimeAt(date))
                || timetableExceptions.hasWorkTimeAt(date);
    }

    /**
     * Returns date range containing given date. This method IS AWARE of timetable exceptions.
     * 
     * <b>Be aware</b> - this method doesn't compose returned date range with the timetable exclusions/inclusions. This means that
     * if you have a shift which works at Monday from 8:00-16:00 and there is defined work time exclusion from 12:00-20:00 and you
     * ask for 10:00 then you will get date range from 8:00-16:00 (as in plan). But if you ask for 14:00 you will get
     * Optional.absent().
     * 
     * @param date
     *            date with time for which work dates range you want to find.
     * @return
     */
    public Optional<DateRange> findWorkTimeAt(final Date date) {
        if (timetableExceptions.hasFreeTimeAt(date)) {
            return Optional.absent();
        }
        DateTime dateTime = new DateTime(date);
        Optional<TimeRange> maybeTimeRangeFromPlan = findWorkTimeAt(dateTime.getDayOfWeek(), dateTime.toLocalTime());
        for (TimeRange timeRangeFromPlan : maybeTimeRangeFromPlan.asSet()) {
            return Optional.of(buildDateRangeFrom(timeRangeFromPlan, date));
        }
        return timetableExceptions.findDateRangeFor(TimetableExceptionType.WORK_TIME, date);
    }

    private DateRange buildDateRangeFrom(final TimeRange timeRange, final Date date) {
        DateTime dateTime = new DateTime(date);
        DateTime midnight = dateTime.withTimeAtStartOfDay();
        DateTime from;
        DateTime to;
        if (timeRange.startsDayBefore()) {
            if (dateTime.toLocalTime().isBefore(timeRange.getFrom())) {
                from = timeRange.getFrom().toDateTime(midnight.minusDays(1));
                to = timeRange.getTo().toDateTime(midnight);
            } else {
                from = timeRange.getFrom().toDateTime(midnight);
                to = timeRange.getTo().toDateTime(midnight.plusDays(1));
            }
        } else {
            from = timeRange.getFrom().toDateTime(midnight);
            to = timeRange.getTo().toDateTime(midnight);
        }
        return new DateRange(from.toDate(), to.toDate());
    }

    /**
     * Returns date range of shift work time that occurs at given day of week and time. This method IS NOT AWARE of timetable
     * exceptions.
     * 
     * @param dayOfWeek
     *            1 == MONDAY !
     * @param time
     * @return shift work time that occurs at given day of week and time
     */
    public Optional<TimeRange> findWorkTimeAt(final int dayOfWeek, final LocalTime time) {
        for (WorkingHours workingHours : workingHoursPerDay.get(dayOfWeek)) {
            Optional<TimeRange> timeRange = workingHours.findRangeFor(time);
            if (timeRange.isPresent()) {
                return timeRange;
            }
        }
        return Optional.absent();
    }

    public Optional<TimeRange> findWorkTimeAt(final int dayOfWeek) {
        for (WorkingHours workingHours : workingHoursPerDay.get(dayOfWeek)) {
            if (!workingHours.getTimeRanges().isEmpty()) {
                return Optional.of(workingHours.getTimeRanges().iterator().next());
            }
        }
        return Optional.absent();
    }

    /**
     * Returns a list with shift work time ranges for whole given day (of the week, to be precise). This method IS NOT AWARE of
     * timetable exceptions, it just check if shift works at given day of week and returns working hours.
     * 
     * @param localDate
     *            date to check
     * @return shift's work time ranges for given date
     * @since 1.4
     */
    public List<TimeRange> findWorkTimeAt(final LocalDate localDate) {
        return Fold.fold(workingHoursPerDay.get(localDate.getDayOfWeek()), Lists.<TimeRange> newArrayList(),
                new BiFunction<List<TimeRange>, WorkingHours, List<TimeRange>>() {

                    @Override
                    public List<TimeRange> apply(final List<TimeRange> acc, final WorkingHours wh) {
                        acc.addAll(wh.getTimeRanges());
                        return acc;
                    }
                });
    }

    /**
     * Returns a copy of the underlying entity.
     * 
     * @return a copy of the underlying entity.
     */
    public Entity getEntity() {
        return shift.copy();
    }

    /**
     * Returns shift's identifier
     * 
     * @return shift's identifier
     */
    public Long getId() {
        return shiftId;
    }

    public DateTime getShiftStartDate() {
        return shiftStartDate;
    }

    public void setShiftStartDate(DateTime shiftStartDate) {
        this.shiftStartDate = shiftStartDate;
    }

    public DateTime getShiftEndDate() {
        return shiftEndDate;
    }

    public void setShiftEndDate(DateTime shiftEndDate) {
        this.shiftEndDate = shiftEndDate;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(shiftId).append(timetableExceptions).append(workingHoursPerDay).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Shift)) {
            return false;
        }

        Shift other = (Shift) obj;

        return new EqualsBuilder().append(shiftId, other.shiftId).append(timetableExceptions, other.timetableExceptions)
                .append(workingHoursPerDay, other.workingHoursPerDay).isEquals();
    }

    private static final class Constants {

        public static final String SUNDAY_LITERAL = "sunday";

        public static final String SATURDAY_LITERAL = "saturday";

        public static final String FRIDAY_LITERAL = "friday";

        public static final String THURSDAY_LITERAL = "thursday";

        // value of this constant refers to name of field in shift model, which currently has a typo..
        public static final String WEDNESDAY_LITERAL = "wensday";

        public static final String TUESDAY_LITERAL = "tuesday";

        public static final String MONDAY_LITERAL = "monday";

        public static final Map<Integer, String> DAYS_OF_WEEK = buildDayNumToNameMap();

        private static Map<Integer, String> buildDayNumToNameMap() {
            Map<Integer, String> dayNumsToDayName = Maps.newHashMapWithExpectedSize(7);
            dayNumsToDayName.put(DateTimeConstants.MONDAY, MONDAY_LITERAL);
            dayNumsToDayName.put(DateTimeConstants.TUESDAY, TUESDAY_LITERAL);
            dayNumsToDayName.put(DateTimeConstants.WEDNESDAY, WEDNESDAY_LITERAL);
            dayNumsToDayName.put(DateTimeConstants.THURSDAY, THURSDAY_LITERAL);
            dayNumsToDayName.put(DateTimeConstants.FRIDAY, FRIDAY_LITERAL);
            dayNumsToDayName.put(DateTimeConstants.SATURDAY, SATURDAY_LITERAL);
            dayNumsToDayName.put(DateTimeConstants.SUNDAY, SUNDAY_LITERAL);
            return Collections.unmodifiableMap(dayNumsToDayName);
        }

        private Constants() {
        };

    }

}
