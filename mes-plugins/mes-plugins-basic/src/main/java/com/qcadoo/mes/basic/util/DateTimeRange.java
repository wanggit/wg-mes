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
package com.qcadoo.mes.basic.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.qcadoo.commons.dateTime.TimeRange;

public class DateTimeRange implements Comparable<DateTimeRange> {

    private final Interval interval;

    public DateTimeRange(final DateTime from, final DateTime to) {
        Preconditions.checkArgument(from != null, "Missing lower bound for date range.");
        Preconditions.checkArgument(to != null, "Missing upper bound for date range.");
        interval = new Interval(from, to);
    }

    public DateTimeRange(final Date from, final Date to) {
        Preconditions.checkArgument(from != null, "Missing lower bound for date range.");
        Preconditions.checkArgument(to != null, "Missing upper bound for date range.");
        DateTime fromDT = new DateTime(from);
        DateTime toDT = new DateTime(to);
        interval = new Interval(fromDT, toDT);
    }

    public DateTimeRange(final DateTime day, final TimeRange range) {
        DateTime from;
        DateTime to;
        if (range.startsDayBefore()) {
            to = range.getTo().toDateTime(day.plusDays(1));
        } else {
            to = range.getTo().toDateTime(day);
        }
        from = range.getFrom().toDateTime(day);
        interval = new Interval(from, to);
    }

    public DateTimeRange(Interval interval) {
        this.interval = interval;
    }

    public boolean contains(final DateTime date) {
        return interval.contains(date);
    }

    public boolean isBefore(final DateTime date) {
        return interval.isBefore(date);
    }

    public boolean isAfter(final DateTime date) {
        return interval.isAfter(date);
    }

    public DateTimeRange trimBefore(final DateTime date) {
        if (contains(date)) {
            return new DateTimeRange(date, interval.getEnd());
        } else if (interval.isBefore(date)) {
            return null;
        } else {
            return this;
        }
    }

    public DateTime getFrom() {
        return interval.getStart();
    }

    public DateTime getTo() {
        return interval.getEnd();
    }

    private DateTimeRange unionWith(DateTimeRange other) {
        Interval otherInterval = other.interval;
        DateTime start = interval.getStart().isBefore(otherInterval.getStart()) ? interval.getStart() : otherInterval.getStart();
        DateTime end = interval.getEnd().isAfter(otherInterval.getEnd()) ? interval.getEnd() : otherInterval.getEnd();
        Interval unionInterval = new Interval(start, end);

        return new DateTimeRange(unionInterval);
    }

    public long durationMillis() {
        return interval.toDurationMillis();
    }

    public long durationInMins() {
        return interval.toDuration().getStandardMinutes();
    }

    public Collection<? extends DateTimeRange> remove(final DateTimeRange range) {
        Interval other = range.interval;
        if (interval.isEqual(other)) {
            return Collections.EMPTY_LIST;
        } else if (interval.contains(other)) {
            if (interval.getStart().isEqual(other.getStart())) {
                return Lists.newArrayList(new DateTimeRange(other.getEnd(), interval.getEnd()));
            } else if (interval.getEnd().isEqual(other.getEnd())) {
                return Lists.newArrayList(new DateTimeRange(interval.getStart(), other.getStart()));
            } else {
                return Lists.newArrayList(new DateTimeRange(interval.getStart(), other.getStart()),
                        new DateTimeRange(other.getEnd(), interval.getEnd()));
            }
        } else if (other.contains(interval)) {
            return Collections.EMPTY_LIST;
        } else if (interval.overlaps(other)) {
            if (interval.getStart().isBefore(other.getStart())) {
                return Lists.newArrayList(new DateTimeRange(interval.getStart(), other.getStart()));
            } else {
                return Lists.newArrayList(new DateTimeRange(other.getEnd(), interval.getEnd()));
            }
        }
        return Lists.newArrayList(this);
    }

    public Collection<? extends DateTimeRange> add(DateTimeRange range) {
        Interval other = range.interval;
        if (interval.contains(other)) {
            return Lists.newArrayList(this);
        } else if (other.contains(interval)) {
            return Lists.newArrayList(range);
        } else if (interval.overlaps(other)) {
            return Lists.newArrayList(unionWith(range));
        }
        return Lists.newArrayList(this, range);
    }

    @Override
    public int compareTo(final DateTimeRange other) {
        return getFrom().compareTo(other.getFrom());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 31).append(getFrom()).append(getTo()).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DateTimeRange other = (DateTimeRange) obj;
        return new EqualsBuilder().append(interval, other.interval).isEquals();
    }

}
