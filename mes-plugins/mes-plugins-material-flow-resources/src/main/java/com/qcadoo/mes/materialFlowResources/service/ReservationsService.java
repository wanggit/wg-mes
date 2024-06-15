package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ReservationFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.tenant.api.MultiTenantCallback;
import com.qcadoo.tenant.api.MultiTenantService;

@Service
public class ReservationsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceReservationsService resourceReservationsService;

    @Autowired
    private MultiTenantService multiTenantService;

    private final static String L_QUANTITY = "quantity";

    public void cleanReservationsTrigger() {
        multiTenantService.doInMultiTenantContext(new MultiTenantCallback() {

            @Override
            public void invoke() {
                cleanReservations();
            }

        });
    }

    public void cleanReservations() {
        String sql = "DELETE FROM materialflowresources_reservation WHERE quantity = 0";
        jdbcTemplate.update(sql, Maps.newHashMap());
    }

    public boolean reservationsEnabledForDocumentPositions(final Entity document) {
        String type = document.getStringField(DocumentFields.TYPE);
        Entity warehouse = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        return DocumentType.isOutbound(type)
                && (warehouse != null && warehouse.getBooleanField(LocationFieldsMFR.DRAFT_MAKES_RESERVATION));
    }

    /**
     * Creates new reservation for position with given id, using specified parameters, and updates resource stock. Uses
     * jdbcTemplate.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding framework method.
     *
     * @param params
     *            map containing keys: id (position id), quantity, product_id, document_id
     * @see ReservationsService#createReservationFromDocumentPosition(Entity)
     */
    public void createReservationFromDocumentPosition(Map<String, Object> params) {
        if (!ReservationsService.this.reservationsEnabledForDocumentPositions(params)) {
            return;
        }
        String query = "INSERT INTO materialflowresources_reservation (location_id, product_id, quantity, position_id, resource_id) "
                + "VALUES ((SELECT locationfrom_id FROM materialflowresources_document WHERE id=:document_id), :product_id, :quantity, :id, :resource_id)";

        jdbcTemplate.update(query, params);
        resourceReservationsService.updateResourceQuantites(params, BigDecimalUtils.convertNullToZero(params.get(L_QUANTITY)));
    }

    /**
     * Creates new reservation for position and updates resource stock. Uses framework.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding jdbc method.
     *
     * @param position
     * @see ReservationsService#createReservationFromDocumentPosition(Map)
     */
    public void createReservationFromDocumentPosition(final Entity position) {
        Entity document = position.getBelongsToField(PositionFields.DOCUMENT);

        if (document != null) {
            if (DocumentState.of(document).equals(DocumentState.ACCEPTED)) {
                return;
            }
            if (!reservationsEnabledForDocumentPositions(document)) {
                return;
            }
            Entity reservation = dataDefinitionService
                    .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESERVATION).create();

            reservation.setField(ReservationFields.LOCATION, document.getBelongsToField(DocumentFields.LOCATION_FROM));
            reservation.setField(ReservationFields.POSITION, position);
            reservation.setField(ReservationFields.PRODUCT, position.getBelongsToField(PositionFields.PRODUCT));
            reservation.setField(ReservationFields.QUANTITY, position.getDecimalField(PositionFields.QUANTITY));
            reservation.setField(ReservationFields.RESOURCE, position.getBelongsToField(PositionFields.RESOURCE));
            reservation = reservation.getDataDefinition().save(reservation);

            position.setField(PositionFields.RESERVATIONS, Lists.newArrayList(reservation));
        }
    }

    /**
     * Updates existing reservation for position with given id, using specified parameters, and updates resource stock. Uses
     * jdbcTemplate.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding framework method.
     *
     * @param params
     *            map containing keys: id (position id), quantity, product_id, document_id
     * @see ReservationsService#updateReservationFromDocumentPosition(Entity)
     */
    public void updateReservationFromDocumentPosition(Map<String, Object> params) {
        if (!ReservationsService.this.reservationsEnabledForDocumentPositions(params)) {
            return;
        }

        if (params.get("id") != null) {
            String queryForOld = "SELECT product_id, resource_id, quantity FROM materialflowresources_position WHERE id = :id";
            Map<String, Object> oldPosition = jdbcTemplate.query(queryForOld, params,
                    new ResultSetExtractor<Map<String, Object>>() {

                        @Override
                        public Map<String, Object> extractData(ResultSet rs) throws SQLException, DataAccessException {
                            Map<String, Object> result = Maps.newHashMap();
                            if (rs.next()) {
                                result.put("product_id", rs.getLong("product_id"));
                                result.put("resource_id", rs.getLong("resource_id"));
                                result.put("quantity", rs.getBigDecimal("quantity"));
                            }
                            return result;
                        }
                    });
            Long newResourceId = (Long) params.get("resource_id");
            Long oldResourceId = (Long) oldPosition.get("resource_id");
            BigDecimal oldPositionQuantity = (BigDecimal) oldPosition.get("quantity");

            BigDecimal newQuantity = BigDecimalUtils.convertNullToZero(params.get(L_QUANTITY));
            BigDecimal quantityToAdd = newQuantity.subtract(oldPositionQuantity);
            String query = "UPDATE materialflowresources_reservation SET "
                    + "location_id = (SELECT locationfrom_id FROM materialflowresources_document WHERE id=:document_id), "
                    + "product_id = :product_id, quantity = :quantity, resource_id = :resource_id WHERE position_id = :id";

            jdbcTemplate.update(query, params);

            if (oldResourceId != null && newResourceId != null) {
                if (oldResourceId.compareTo(newResourceId) != 0) {
                    resourceReservationsService.updateResourceQuantites(params, newQuantity);
                    Map<String, Object> paramsForOld = Maps.newHashMap(params);
                    paramsForOld.put("resource_id", oldResourceId);
                    resourceReservationsService.updateResourceQuantites(paramsForOld, oldPositionQuantity.negate());
                } else {
                    resourceReservationsService.updateResourceQuantites(params, quantityToAdd);
                }
            } else if (oldResourceId == null && newResourceId != null) {
                resourceReservationsService.updateResourceQuantites(params, newQuantity);
            } else if (oldResourceId != null) {
                Map<String, Object> paramsForOld = Maps.newHashMap(params);
                paramsForOld.put("resource_id", oldResourceId);
                resourceReservationsService.updateResourceQuantites(paramsForOld, oldPositionQuantity.negate());
            }
        }

    }

    /**
     * Updates reservation for position and updates resource stock. Uses framework.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding jdbc method.
     *
     * @param position
     * @see ReservationsService#updateReservationFromDocumentPosition(Map)
     */
    public void updateReservationFromDocumentPosition(final Entity position) {
        Entity document = position.getBelongsToField(PositionFields.DOCUMENT);

        if (document != null) {

            if (!reservationsEnabledForDocumentPositions(position.getBelongsToField(PositionFields.DOCUMENT))) {
                return;
            }

            Entity product = position.getBelongsToField(PositionFields.PRODUCT);
            Entity location = position.getBelongsToField(PositionFields.DOCUMENT).getBelongsToField(DocumentFields.LOCATION_FROM);
            Entity resource = position.getBelongsToField(PositionFields.RESOURCE);
            BigDecimal newQuantity = position.getDecimalField(PositionFields.QUANTITY);

            Entity existingReservation = getReservationForPosition(position);

            if (existingReservation != null) {
                existingReservation.setField(ReservationFields.QUANTITY, newQuantity);
                existingReservation.setField(ReservationFields.PRODUCT, product);
                existingReservation.setField(ReservationFields.LOCATION, location);
                existingReservation.setField(ReservationFields.RESOURCE, resource);

                existingReservation.getDataDefinition().save(existingReservation);
            }
        }
    }

    /**
     * Deletes reservation for position with given id and updates resource stock. Uses jdbcTemplate.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding framework method.
     *
     * @param params
     *            map containing keys: id (position id), quantity, product_id, document_id
     * @see ReservationsService#deleteReservationFromDocumentPosition(Entity)
     */
    public void deleteReservationFromDocumentPosition(Map<String, Object> params) {
        if (!reservationsEnabledForDocumentPositions(params)) {
            return;
        }
        String query = "DELETE FROM materialflowresources_reservation WHERE position_id = :id";
        jdbcTemplate.update(query, params);
        resourceReservationsService.updateResourceQuantites(params,
                BigDecimalUtils.convertNullToZero(params.get(L_QUANTITY)).negate());
    }

    /**
     * Deletes reservation for position and updates resource stock. Uses framework.
     *
     * Warning! If logic in this method is changed, it should also be applied to corresponding jdbc method.
     *
     * @param position
     * @see ReservationsService#deleteReservationFromDocumentPosition(Map)
     */
    public void deleteReservationFromDocumentPosition(final Entity position) {
        if (!reservationsEnabledForDocumentPositions(position.getBelongsToField(PositionFields.DOCUMENT))) {
            return;
        }

        Entity reservation = getReservationForPosition(position);
        if (reservation != null) {
            reservation.getDataDefinition().delete(reservation.getId());
        }
    }

    public Boolean reservationsEnabledForDocumentPositions(Map<String, Object> params) {
        String queryForDocumentType = "SELECT type, locationfrom_id FROM materialflowresources_document WHERE id = :document_id";
        Map<String, Object> documentMap = jdbcTemplate.queryForMap(queryForDocumentType, params);
        if (DocumentType.isOutbound((String) documentMap.get("type"))) {
            String query = "SELECT draftmakesreservation FROM materialflow_location WHERE id = :location_id";
            Boolean enabled = jdbcTemplate.queryForObject(query,
                    Collections.singletonMap("location_id", (Long) documentMap.get("locationfrom_id")), Boolean.class);
            return enabled;
        } else {
            return false;
        }
    }

    public Entity getReservationForPosition(final Entity position) {
        if (position.getId() == null) {
            return null;
        }
        return dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESERVATION).find()
                .add(SearchRestrictions.belongsTo(ReservationFields.POSITION, position)).setMaxResults(1).uniqueResult();
    }

}
