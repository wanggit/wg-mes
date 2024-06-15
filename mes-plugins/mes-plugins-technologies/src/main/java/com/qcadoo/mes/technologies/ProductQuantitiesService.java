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
package com.qcadoo.mes.technologies;

import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface ProductQuantitiesService {

    ProductQuantitiesHolder getProductComponentQuantities(final Entity technology, final BigDecimal givenQuantity);

    /**
     * @param order
     *            Order
     * @return Map with operationProductComponents (in or out) as the keys and its quantities as the values. Be aware that
     *         products that are the same, but are related to different operations are here as different entries.
     */
    OperationProductComponentWithQuantityContainer getProductComponentQuantities(final Entity order);

    /**
     * @param orders
     *            Given list of orders
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    OperationProductComponentWithQuantityContainer getProductComponentQuantitiesWithoutNonComponents(final List<Entity> orders,
            final boolean onTheFly);

    /**
     * @param orders
     *            Given list of orders
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    OperationProductComponentWithQuantityContainer getProductComponentQuantitiesWithoutNonComponents(final List<Entity> orders);

    /**
     * @param technology
     *            Given technology
     * @param givenQuantity
     *            How many products, that are outcomes of this technology, we want.
     * @param mrpAlgorithm
     *            MRP Algorithm
     * @return Map with product as the key and its quantity as the value. This time keys are products, so they are aggregated.
     */
    Map<Long, BigDecimal> getNeededProductQuantities(final Entity technology, final BigDecimal givenQuantity,
            final MrpAlgorithm mrpAlgorithm);

    Map<OperationProductComponentHolder, BigDecimal> getNeededProductQuantitiesByOPC(final Entity technology,
            final BigDecimal givenQuantity, final MrpAlgorithm mrpAlgorithm);

    Map<OperationProductComponentHolder, BigDecimal> getNeededProductQuantitiesByOPC(final Entity technology,
            final Entity product, final BigDecimal givenQuantity, final MrpAlgorithm mrpAlgorithm);

    Map<OperationProductComponentHolder, BigDecimal> getNeededProductQuantities(final Entity technology, final Entity product,
            final BigDecimal givenQuantity);

    /**
     * @param order
     *            Order
     * @param mrpAlgorithm
     *            MRP Algorithm
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    Map<Long, BigDecimal> getNeededProductQuantities(final Entity order, final MrpAlgorithm mrpAlgorithm);

    /**
     * @param orders
     *            Given list of orders
     * @param mrpAlgorithm
     *            MRP Algorithm
     * @param onTheFly
     *            onTheFly
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    Map<Long, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final MrpAlgorithm mrpAlgorithm,
            final boolean onTheFly);

    /**
     * @param components
     *            List of components that have order as belongsTo relation
     * @param mrpAlgorithm
     *            MRP Algorithm
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    Map<Long, BigDecimal> getNeededProductQuantitiesForComponents(final List<Entity> components, final MrpAlgorithm mrpAlgorithm);

    /**
     * @param orders
     *            orders
     * @param operationRuns
     *            Method takes an empty map and puts here info on how many times certain operation (operationComponent) has to be
     *            run.
     * @param nonComponents
     *            non components
     * @return Map with operationProductComponents (in or out) as the keys and its quantities as the values. Be aware that
     *         products that are the same, but are related to different operations are here as different entries.
     */
    OperationProductComponentWithQuantityContainer getProductComponentWithQuantities(final List<Entity> orders,
            final Map<Long, BigDecimal> operationRuns, final Set<OperationProductComponentHolder> nonComponents);

    /**
     * @param productComponentQuantity
     *            Product Component Quantity
     * @param productQuantities
     *            Product Quantities
     */
    void addProductQuantitiesToList(final Entry<OperationProductComponentHolder, BigDecimal> productComponentQuantity,
            final Map<Long, BigDecimal> productQuantities);

    /**
     * @param technology
     * @param product
     * @param givenQuantity
     * @param operationRuns
     * @param nonComponents
     * @return
     */
    OperationProductComponentWithQuantityContainer getProductComponentWithQuantitiesForTechnology(final Entity technology,
            final Entity product, final BigDecimal givenQuantity, final Map<Long, BigDecimal> operationRuns,
            final Set<OperationProductComponentHolder> nonComponents);

    /**
     * @param productComponentWithQuantitiesForOrders
     * @return
     */
    OperationProductComponentWithQuantityContainer groupOperationProductComponentWithQuantities(
            final Map<Long, OperationProductComponentWithQuantityContainer> productComponentWithQuantitiesForOrders);

    /**
     * Gets technology operation component
     *
     * @param technologyOperationComponentId
     * @return technology operation component
     */
    Entity getTechnologyOperationComponent(final Long technologyOperationComponentId);

    /**
     * Gets product
     *
     * @param productId
     * @return product
     */
    Entity getProduct(final Long productId);

    void traverseProductQuantitiesAndOperationRuns(Entity technology, BigDecimal givenQuantity, Entity operationComponent,
            Entity previousOperationComponent,
            OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer,
            Set<OperationProductComponentHolder> nonComponents, Map<Long, BigDecimal> operationRuns);

    void traverseProductQuantitiesAndOperationRuns(Entity technology, Map<Long, Entity> entitiesById, BigDecimal givenQuantity,
            Entity operationComponent, Entity previousOperationComponent,
            OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer,
            Set<OperationProductComponentHolder> nonComponents, Map<Long, BigDecimal> operationRuns);

    void preloadOperationProductComponentQuantity(List<Entity> operationProductComponents,
            OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer);

    void preloadProductQuantitiesAndOperationRuns(EntityTree operationComponents,
            OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer,
            Map<Long, BigDecimal> operationRuns);

    Map<Long, BigDecimal> getProductWithQuantities(OperationProductComponentWithQuantityContainer productComponentWithQuantities,
            Set<OperationProductComponentHolder> nonComponents, MrpAlgorithm mrpAlgorithm,
            String operationProductComponentModelName);

}
