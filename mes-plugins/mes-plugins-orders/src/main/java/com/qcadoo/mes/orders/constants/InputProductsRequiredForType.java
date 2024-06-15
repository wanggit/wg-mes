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
package com.qcadoo.mes.orders.constants;

public enum InputProductsRequiredForType {
    START_OPERATIONAL_TASK("02startOperationalTask");

    private final String inputProductsRequiredForType;

    InputProductsRequiredForType(final String inputProductsRequiredForType) {
        this.inputProductsRequiredForType = inputProductsRequiredForType;
    }

    public String getStringValue() {
        return inputProductsRequiredForType;
    }

    public static InputProductsRequiredForType parseString(final String string) {
        if ("02startOperationalTask".equals(string)) {
            return START_OPERATIONAL_TASK;
        }

        throw new IllegalStateException("Unsupported InputProductsRequiredForType: " + string);
    }

}
