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
package com.qcadoo.mes.deliveries;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.deliveries.constants.CompanyProductFields;
import com.qcadoo.mes.deliveries.constants.ProductFieldsD;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

@Service
public class CompanyProductServiceImpl implements CompanyProductService {

    public boolean checkIfProductIsNotUsed(final Entity companyProduct) {
        if (companyProduct.getId() == null) {
            Entity product = companyProduct.getBelongsToField(CompanyProductFields.PRODUCT);

            if (product == null) {
                return true;
            } else {
                Entity company = companyProduct.getBelongsToField(CompanyProductFields.COMPANY);

                if (company == null) {
                    return true;
                } else {
                    SearchResult searchResult = company.getHasManyField(CompanyFieldsD.PRODUCTS).find()
                            .add(SearchRestrictions.belongsTo(CompanyProductFields.PRODUCT, product)).list();

                    return searchResult.getEntities().isEmpty();
                }
            }
        }

        return true;
    }

    public boolean checkIfDefaultAlreadyExists(final Entity companyProduct) {
        if (companyProduct.getBooleanField(CompanyProductFields.IS_DEFAULT)) {
            Entity product = companyProduct.getBelongsToField(CompanyProductFields.PRODUCT);

            if (product == null) {
                return false;
            } else {
                List<Entity> companyProductsForProduct = product.getHasManyField(ProductFieldsD.PRODUCT_COMPANIES);
                return companyProductsForProduct.stream().anyMatch(
                        companyProductForProduct -> companyProductForProduct.getBooleanField(CompanyProductFields.IS_DEFAULT)
                                && !companyProductForProduct.getId().equals(companyProduct.getId()));
            }
        }

        return false;
    }
}
