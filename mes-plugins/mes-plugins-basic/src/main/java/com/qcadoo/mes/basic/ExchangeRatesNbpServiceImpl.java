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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExchangeRatesNbpServiceImpl implements ExchangeRatesNbpService {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeRatesNbpServiceImpl.class);

    @Override
    public Map<String, BigDecimal> get(NbpProperties nbpProperties) {
        try {
            final InputStream input = new URL(nbpProperties.getUrl()).openStream();
            return parse(input, nbpProperties);
        } catch (IOException e) {
            LOG.error("Reading URL stream failed", e);
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, BigDecimal> parse(InputStream inputStream, NbpProperties nbpProperties) {
        Map<String, BigDecimal> exRates = new HashMap<>();
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader sr = inputFactory.createXMLStreamReader(inputStream);
            sr.nextTag(); // advance to tabela_kursow
            sr.nextTag(); // advance to numer_tabeli

            String currencyCode = "";
            BigDecimal exRate = null;
            BigDecimal factor = BigDecimal.ONE;
            String exchangeRateField = nbpProperties.fieldName();
            while (sr.hasNext()) {
                if (sr.getEventType() == XMLStreamConstants.END_DOCUMENT) {
                    sr.close();
                }
                if (sr.isStartElement()) {
                    String s = sr.getLocalName();

                    if (s.equals("przelicznik")) {
                        factor = new BigDecimal(sr.getElementText().replace(',', '.'));
                    } else if (s.equals("kod_waluty")) {
                        currencyCode = sr.getElementText();
                    } else if (s.equals(exchangeRateField)) {
                        exRate = new BigDecimal(sr.getElementText().replace(',', '.'));
                    }

                    if (exRate != null) {
                        exRates.put(currencyCode, exRate.divide(factor, RoundingMode.HALF_UP));
                        exRate = null;
                    }
                }
                sr.next();
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        } finally {
            closeStream(inputStream);
        }
        return exRates;
    }

    private void closeStream(Closeable s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (IOException e) {
            LOG.error("Cannot close URL Stream");
        }
    }

}
