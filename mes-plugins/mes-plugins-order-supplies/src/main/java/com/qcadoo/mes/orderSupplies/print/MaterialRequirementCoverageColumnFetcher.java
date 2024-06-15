/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.orderSupplies.print;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orderSupplies.OrderSuppliesService;
import com.qcadoo.mes.orderSupplies.constants.ColumnForCoveragesFields;
import com.qcadoo.model.api.Entity;

@Service
public class MaterialRequirementCoverageColumnFetcher {

    @Autowired
    private OrderSuppliesService orderSuppliesService;

    @Autowired
    private ApplicationContext applicationContext;

    public Map<Entity, Map<String, String>> getCoverageProductsColumnValues(final List<Entity> coverageProducts) {
        Map<Entity, Map<String, String>> coverageProductsColumnValues = new HashMap<Entity, Map<String, String>>();

        fetchColumnValues(coverageProductsColumnValues, "getCoverageProductsColumnValues", coverageProducts);

        return coverageProductsColumnValues;
    }

    @SuppressWarnings("unchecked")
    private void fetchColumnValues(final Map<Entity, Map<String, String>> columnValues, final String methodName,
            final List<Entity> requestForQuotationProducts) {
        List<Entity> columnsForOrderCoverages = orderSuppliesService.getColumnsForCoverages();

        Set<String> classNames = new HashSet<String>();

        for (Entity columnForCoverages : columnsForOrderCoverages) {
            String className = columnForCoverages.getStringField(ColumnForCoveragesFields.COLUMN_FILLER);
            classNames.add(className);
        }

        for (String className : classNames) {
            Class<?> clazz;
            try {
                clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Failed to find class: " + className, e);
            }

            Object bean = applicationContext.getBean(clazz);

            if (bean == null) {
                throw new IllegalStateException("Failed to find bean for class: " + className);
            }

            Method method;

            try {
                method = clazz.getMethod(methodName, List.class);
            } catch (SecurityException e) {
                throw new IllegalStateException("Failed to find column evaulator method in class: " + className, e);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Failed to find column evaulator method in class: " + className, e);
            }

            Map<Entity, Map<String, String>> values;

            String invokeMethodError = "Failed to invoke column evaulator method";
            try {
                values = (Map<Entity, Map<String, String>>) method.invoke(bean, requestForQuotationProducts);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(invokeMethodError, e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(invokeMethodError, e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(invokeMethodError, e);
            }

            for (Entry<Entity, Map<String, String>> entry : values.entrySet()) {
                if (columnValues.containsKey(entry.getKey())) {
                    for (Entry<String, String> deepEntry : entry.getValue().entrySet()) {
                        columnValues.get(entry.getKey()).put(deepEntry.getKey(), deepEntry.getValue());
                    }
                } else {
                    columnValues.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

}
