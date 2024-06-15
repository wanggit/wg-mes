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
package com.qcadoo.mes.materialFlowResources.costNorms.dao.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.costNorms.dao.CostNormsDao;
import com.qcadoo.mes.materialFlowResources.costNorms.dao.model.CostNorm;

@Repository
public class CostNormsDaoImpl implements CostNormsDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<CostNorm> getLastPurchaseCostsForProducts(final List<Long> productIds, final List<Long> warehousesIds) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT DISTINCT p.product_id AS productId, p.price AS lastPurchaseCost, d.time ");
        queryBuilder.append("FROM materialflowresources_position p ");
        queryBuilder.append("JOIN materialflowresources_document d ON d.id = p.document_id ");
        queryBuilder.append("JOIN  ");
        queryBuilder.append("   (SELECT pos.product_id AS lastId, max(doc.time) AS lastTime ");
        queryBuilder.append("       FROM materialflowresources_position pos ");
        queryBuilder.append("       JOIN materialflowresources_document doc ");
        queryBuilder.append("       ON doc.id = pos.document_id ");
        queryBuilder.append("       AND doc.type IN ('01receipt', '02internalInbound') AND doc.state = '02accepted' ");

        if (!warehousesIds.isEmpty()) {
            queryBuilder.append("AND doc.locationto_id IN (:warehousesIds) ");
        }

        queryBuilder.append("   WHERE pos.price IS NOT NULL ");
        queryBuilder.append("   GROUP BY pos.product_id) AS p2 ");
        queryBuilder.append("ON p.product_id = p2.lastId AND d.time = p2.lastTime ");

        if (!productIds.isEmpty()) {
            queryBuilder.append("WHERE p.product_id IN (:productIds) ");
        }

        queryBuilder.append("ORDER BY p.product_id");

        SqlParameterSource namedParameters = new MapSqlParameterSource("productIds", productIds).addValue("warehousesIds",
                warehousesIds);

        List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(queryBuilder.toString(), namedParameters);

        List<CostNorm> lastPurchases = Lists.newArrayList();

        queryForList.forEach(new Consumer<Map<String, Object>>() {

            @Override
            public void accept(Map<String, Object> stringObjectMap) {
                CostNorm costNorm = new CostNorm();

                costNorm.setProductId((Long) stringObjectMap.get("productId"));
                costNorm.setLastPurchaseCost((BigDecimal) stringObjectMap.get("lastPurchaseCost"));

                lastPurchases.add(costNorm);
            }
        });

        return lastPurchases;
    }

    @Override
    public List<CostNorm> getAverageCostForProducts(final List<Long> productIds, final List<Long> warehousesIds) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT r.product_id AS productId, SUM(r.price * r.quantity)/SUM(r.quantity) AS averageCost ");
        queryBuilder.append("FROM materialflowresources_resource r WHERE r.price IS NOT NULL ");

        if (!productIds.isEmpty()) {
            queryBuilder.append("AND r.product_id IN (:productIds) ");
        }
        if (!warehousesIds.isEmpty()) {
            queryBuilder.append("AND r.location_id IN (:warehousesIds) ");
        }

        queryBuilder.append("GROUP BY r.product_id");

        SqlParameterSource namedParameters = new MapSqlParameterSource("productIds", productIds).addValue("warehousesIds",
                warehousesIds);

        List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(queryBuilder.toString(), namedParameters);

        List<CostNorm> averageCosts = Lists.newArrayList();

        queryForList.forEach(new Consumer<Map<String, Object>>() {

            @Override
            public void accept(Map<String, Object> stringObjectMap) {
                CostNorm costNorm = new CostNorm();

                costNorm.setProductId((Long) stringObjectMap.get("productId"));
                costNorm.setAverageCost((BigDecimal) stringObjectMap.get("averageCost"));

                averageCosts.add(costNorm);
            }
        });

        return averageCosts;
    }

    @Override
    public void updateCostNormsForProducts(final Collection<CostNorm> costNorms) {
        for (CostNorm costNorm : costNorms) {
            StringBuilder queryBuilder = new StringBuilder();

            queryBuilder.append("UPDATE basic_product SET ");
            queryBuilder.append(prepareValuesToUpdate(costNorm));
            queryBuilder.append(" WHERE id = :productId");

            SqlParameterSource namedParameters = new MapSqlParameterSource("productId", costNorm.getProductId())
                    .addValue("lastPurchaseCost", costNorm.getLastPurchaseCost())
                    .addValue("averageCost", costNorm.getAverageCost()).addValue("nominalCost", costNorm.getNominalCost())
                    .addValue("costForNumber", costNorm.getCostForNumber());

            jdbcTemplate.update(queryBuilder.toString(), namedParameters);
        }
    }

    private String prepareValuesToUpdate(final CostNorm costNorm) {
        StringBuilder values = new StringBuilder();

        if (Objects.nonNull(costNorm.getLastPurchaseCost())) {
            values.append("lastpurchasecost = :lastPurchaseCost");
        }

        if (Objects.nonNull(costNorm.getAverageCost())) {
            if (values.length() > 0) {
                values.append(", ");
            }

            values.append("averagecost = :averageCost");
        }

        if (Objects.nonNull(costNorm.getCostForNumber())) {
            if (values.length() > 0) {
                values.append(", ");
            }

            values.append("costfornumber = :costForNumber");
        }

        if (Objects.nonNull(costNorm.getNominalCost())) {
            if (values.length() > 0) {
                values.append(", ");
            }

            values.append("nominalcost = :nominalCost");
        }

        return values.toString();
    }

}
