package com.qcadoo.mes.basic;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.security.api.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LookupUtils {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    protected TranslationService translationService;

    @Autowired
    protected SecurityService securityService;

    @Value("${useCompressedStaticResources}")
    protected boolean useCompressedStaticResources;

    public <R> GridResponse<R> getGridResponse(String query, String sidx, String sord, Integer page, int perPage, R recordExample) {
        return getGridResponse(query, sidx, sord, page, perPage, recordExample, new HashMap<>());
    }

    public <R> GridResponse<R> getGridResponse(String query, String sidx, String sord, Integer page, int perPage, R recordExample, Map<String, Object> parameters) {
        sidx = sidx != null ? sidx.toLowerCase() : "";
        sord = sord != null ? sord.toLowerCase() : "";

        Preconditions.checkState(Arrays.asList("asc", "desc", "").contains(sord));
        Preconditions.checkState(Arrays.asList(recordExample.getClass().getDeclaredFields()).stream().map(Field::getName).map(String::toLowerCase).collect(Collectors.toList()).contains(sidx));

        query += addQueryWhereForObject(recordExample);

        parameters.putAll(getParametersForObject(recordExample));

        String queryCount = String.format(query, "COUNT(*)", "");
        String queryRecords = String.format(query, "*", "ORDER BY " + sidx + " " + sord) + String.format(" LIMIT %d OFFSET %d", perPage, perPage * (page - 1));

        Integer countRecords = jdbcTemplate.queryForObject(queryCount, parameters, Long.class).intValue();
        List<R> records = jdbcTemplate.query(queryRecords, parameters, new BeanPropertyRowMapper(recordExample.getClass()));

        return new GridResponse<>(page, Double.valueOf(Math.ceil((1.0 * countRecords) / perPage)).intValue(), countRecords, records);
    }

    public String addQueryWhereForObject(Object object) {
        List<String> items = new ArrayList<>();

        if (object != null) {
            for (Field field : object.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(object);
                    if (value != null) {
                        if (value instanceof Number) {
                            items.add(String.format("%s = :%s", field.getName(), field.getName()));

                        } else if (value instanceof Date) {
                            items.add(String.format("%s = :%s", field.getName(), field.getName()));

                        } else if (value instanceof Boolean) {
                            items.add(String.format("%s = :%s", field.getName(), field.getName()));

                        } else if (value instanceof String) {
                            SearchAttribute.SEARCH_TYPE searchType = field.isAnnotationPresent(SearchAttribute.class) ? field.getAnnotation(SearchAttribute.class).searchType() : SearchAttribute.SEARCH_TYPE.LIKE;

                            if (searchType == SearchAttribute.SEARCH_TYPE.EXACT_MATCH) {
                                items.add(String.format("lower(%s) = lower(:%s)", field.getName(), field.getName()));

                            } else {
                                items.add(String.format("%s ilike :%s", field.getName(), field.getName()));
                            }
                        }
                    }

                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        String where = "";

        if (!items.isEmpty()) {
            where = " WHERE " + items.stream().collect(Collectors.joining(" AND "));
        }

        return where;
    }

    public Map<String, Object> getParametersForObject(Object object) {
        Map<String, Object> parameters = new HashMap<>();

        if (object != null) {
            for (Field field : object.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(object);
                    if (value != null) {
                        if (value instanceof String) {
                            SearchAttribute.SEARCH_TYPE searchType = field.isAnnotationPresent(SearchAttribute.class) ? field.getAnnotation(SearchAttribute.class).searchType() : SearchAttribute.SEARCH_TYPE.LIKE;

                            if (searchType == SearchAttribute.SEARCH_TYPE.EXACT_MATCH) {

                                parameters.put(field.getName(), value);

                            } else {
                                String ilikeValue = "%" + value + "%";
                                ilikeValue = ilikeValue.replace("*", "%");
                                ilikeValue = ilikeValue.replace("%%", "%");
                                parameters.put(field.getName(), ilikeValue);
                            }

                        } else {
                            parameters.put(field.getName(), value);
                        }
                    }

                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        return parameters;
    }

    public ModelAndView getModelAndView(final String recordName, final String view, final Locale locale) {
        ModelAndView mav = new ModelAndView();

        mav.addObject("userLogin", securityService.getCurrentUserName());
        mav.addObject("translationsMap", translationService.getMessagesGroup("documentGrid", locale));
        mav.addObject("recordName", recordName);

        mav.setViewName("basic/" + view);
        mav.addObject("useCompressedStaticResources", useCompressedStaticResources);
        return mav;
    }

    public Map<String, Object> getConfigMap(List<String> columns) {
        Map<String, Object> config = new HashMap<>();

        Map<String, Object> modelId = new HashMap<>();
        modelId.put("name", "id");
        modelId.put("index", "id");
        modelId.put("key", true);
        modelId.put("hidden", true);

        Map<String, Map<String, Object>> colModel = new LinkedHashMap<>();
        colModel.put("ID", modelId);

        columns.forEach(column -> {
            Map<String, Object> model = new HashMap<>();
            model.put("name", column);
            model.put("index", column);
            model.put("editable", false);

            Map<String, Object> editoptions = new HashMap<>();
            editoptions.put("readonly", "readonly");
            model.put("editoptions", editoptions);

            Map<String, Object> searchoptions = new HashMap<>();
            model.put("searchoptions", searchoptions);

            colModel.put(column, model);
        });

        config.put("colModel", colModel.values());
        config.put("colNames", colModel.keySet());

        return config;
    }

    public List<Long> parseIds(final String ids) {
        List<Long> result = Lists.newArrayList();
        String[] splittedIds = ids.replace("[", "").replace("]", "").replace("\"", "").split(",");
        for (String splittedId : splittedIds) {
            result.add(Long.parseLong(splittedId));
        }
        return result;
    }
}
