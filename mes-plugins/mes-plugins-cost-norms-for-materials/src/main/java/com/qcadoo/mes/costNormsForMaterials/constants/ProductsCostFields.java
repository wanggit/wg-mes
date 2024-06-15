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
package com.qcadoo.mes.costNormsForMaterials.constants;

import org.apache.commons.lang3.StringUtils;

public enum ProductsCostFields {

    NOMINAL("01nominal", "nominalCost"), AVERAGE("02average", "averageCost"), LAST_PURCHASE("03lastPurchase",
            "lastPurchaseCost"), AVERAGE_OFFER_COST("04averageOfferCost", "averageOfferCost"), LAST_OFFER_COST("05lastOfferCost",
            "lastOfferCost"), COST_FOR_ORDER("06costForOrder", "costForOrder"), OFFER_COST_OR_LAST_PURCHASE("07offerCostOrLastPurchase", "lastPurchaseCost");

    private final String mode;

    private final String strValue;

    ProductsCostFields(final String mode, final String strValue) {
        this.mode = mode;
        this.strValue = strValue;
    }

    public String getMode() {
        return mode;
    }

    public String getStrValue() {
        return strValue;
    }

    public static ProductsCostFields forMode(final String mode) {
        for (ProductsCostFields productsCostFields : values()) {
            if (StringUtils.equalsIgnoreCase(mode, productsCostFields.getMode())) {
                return productsCostFields;
            }
        }
        throw new IllegalStateException("Unsupported materialCostsUsed: " + mode);
    }
}
