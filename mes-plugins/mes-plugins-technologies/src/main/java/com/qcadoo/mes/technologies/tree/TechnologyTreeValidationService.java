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
package com.qcadoo.mes.technologies.tree;

import java.util.Map;
import java.util.Set;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

/**
 * Validation service for technology operation's tree.
 * 
 * @since 1.1.6
 */
public interface TechnologyTreeValidationService {

    /**
     * @param technologyTree
     *            tree structure of operation to be validates
     * @return {@link Map} of parent node number mapped to {@link Set} of node numbers of children operations which produce more
     *         than one parent's input products.
     */
    Map<String, Set<String>> checkConsumingManyProductsFromOneSubOp(EntityTree technologyTree);

    /**
     * @param technologyTree
     *            tree structure of operation to be validated
     * @return A map where parent's node number is a key, and a set of maps where each map represents single violation. than one
     *         parent's input products.
     */
    Map<String, Set<Entity>> checkConsumingTheSameProductFromManySubOperations(EntityTree technologyTree);

}
