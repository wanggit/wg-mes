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
package com.qcadoo.mes.basic;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;

public interface ExchangeRatesNbpService {

    String CRON_LAST_ALL = "0 20 13 ? * MON-FRI";     // working days  11:45 - 12:15 -> 12:20

    Map<String, BigDecimal> get(NbpProperties nbpProperties);

    Map<String, BigDecimal> parse(InputStream inputStream, NbpProperties nbpProperties);

    enum NbpProperties {

        LAST_A("LastA"){
            @Override
            public String fieldName() {
                return "kurs_sredni";
            }
        },

        LAST_B("LastB"){
            @Override
            public String fieldName() {
                return "kurs_sredni";
            }
        };

        private final String xmlFileName;

        NbpProperties(String xmlFileName) {
            this.xmlFileName = xmlFileName;
        }

        public String getUrl() {
            return "https://static.nbp.pl/dane/kursy/xml/" + xmlFileName + ".xml";
        }

        public abstract String fieldName();
    }
}
