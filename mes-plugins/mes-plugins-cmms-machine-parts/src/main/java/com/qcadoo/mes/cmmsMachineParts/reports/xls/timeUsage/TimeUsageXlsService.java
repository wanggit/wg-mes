package com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.factory.EventFieldsForTypeFactory;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForType;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage.dto.TimeUsageDTO;
import com.qcadoo.mes.cmmsMachineParts.reports.xls.timeUsage.dto.TimeUsageGroupDTO;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service public class TimeUsageXlsService {

    @Autowired private TranslationService translationService;

    @Autowired
    private TimeUsageXlsDataProvider timeUsageXLSDataProvider;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private EventFieldsForTypeFactory fieldsForTypeFactory;

    private ReportStyleFactory reportStyleFactory;

    public String getReportTitle(final Locale locale) {
        return translationService.translate(TimeUsageXlsConstants.REPORT_TITLE, locale);
    }

    public void buildExcelContent(final HSSFWorkbook workbook, final HSSFSheet sheet, Map<String, Object> filters,
            final Locale locale) {
        reportStyleFactory = new ReportStyleFactory(workbook);
        List<TimeUsageDTO> usages = timeUsageXLSDataProvider.getUsages((Map<String, Object>) filters.get("filtersMap"));
        updatePartsAndDescription(usages, locale);
        fillHeaderData(workbook, sheet, locale, (Map<String, Object>) filters.get("filtersMap"));
        fillHeaderRow(workbook, sheet, locale);
        List<TimeUsageGroupDTO> timeUsageGroups = group(usages);
        int rowCounter = 5;
        for (TimeUsageGroupDTO timeUsageGroupDTO : timeUsageGroups) {
            rowCounter = fillTimeUsageRows(workbook, sheet, timeUsageGroupDTO, rowCounter++, locale);
        }
        setColumnsWidths(sheet);
    }

    private void updatePartsAndDescription(List<TimeUsageDTO> usages, Locale locale) {
        for (TimeUsageDTO usage : usages) {
            if ("planned".equals(usage.getEventType())) {
                PlannedEventType type = PlannedEventType.parseString(usage.getType());
                FieldsForType fields = fieldsForTypeFactory.createFieldsForType(type);
                if (fields.getHiddenTabs().contains(PlannedEventFields.MACHINE_PARTS_TAB)) {
                    String notApplicable = translationService.translate("cmmsMachineParts.timeUsageReport.na", locale);
                    usage.setParts(notApplicable);
                }
                if (fields.getHiddenTabs().contains(PlannedEventFields.SOLUTION_DESCRIPTION_TAB)) {
                    String notApplicable = translationService.translate("cmmsMachineParts.timeUsageReport.na", locale);
                    usage.setDescription(notApplicable);
                }
            }
        }
    }

    private void setColumnsWidths(HSSFSheet sheet) {
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 3500);
        sheet.setColumnWidth(2, 3500);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 4000);
        sheet.setColumnWidth(5, 2500);
        sheet.setColumnWidth(6, 5000);
        sheet.setColumnWidth(7, 5000);
        sheet.setColumnWidth(8, 4000);
        sheet.setColumnWidth(9, 4000);
        sheet.setColumnWidth(10, 4500);
        sheet.setColumnWidth(11, 5000);
    }

    private List<TimeUsageGroupDTO> group(List<TimeUsageDTO> usages) {
        List<TimeUsageGroupDTO> groups = Lists.newLinkedList();
        Map<String, List<TimeUsageDTO>> workerMap = usages.stream().collect(Collectors.groupingBy(TimeUsageDTO::getWorker));
        for (Entry<String, List<TimeUsageDTO>> entry : workerMap.entrySet()) {
            Map<Date, List<TimeUsageDTO>> dateMap = entry.getValue().stream()
                    .collect(Collectors.groupingBy(TimeUsageDTO::getStartDate));
            for (Date date : dateMap.keySet()) {
                TimeUsageGroupDTO timeUsageGroup = new TimeUsageGroupDTO(date, entry.getKey(), dateMap.get(date));
                groups.add(timeUsageGroup);
            }
        }
        return groups.stream().sorted((g1, g2) -> {
            if (g1.getWorker().equals(g2.getWorker())) {
                return g1.getDate().compareTo(g2.getDate());
            } else {
                return g1.getWorker().compareTo(g2.getWorker());
            }
        }).collect(Collectors.toList());
    }

    private void fillHeaderData(final HSSFWorkbook workbook, final HSSFSheet sheet, final Locale locale,
                                Map<String, Object> filters) {
        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);

        HSSFRow titleRow = sheet.createRow(0);
        HSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(translationService.translate("cmmsMachineParts.timeUsageReport.report.title", locale));
        titleCell.setCellStyle(style);

        HSSFRow datesRow = sheet.createRow(1);
        HSSFCell startingFromLabelCell = datesRow.createCell(0);
        startingFromLabelCell.setCellValue(translationService.translate("cmmsMachineParts.timeUsageReport.report.startingFrom",
                locale));
        startingFromLabelCell.setCellStyle(style);
        if (filters.containsKey("fromDate")) {
            HSSFCell startingFromCell = datesRow.createCell(1);
            startingFromCell.setCellValue(getDateOnly((Date) filters.get("fromDate")));
        }
        HSSFCell toLabelCell = datesRow.createCell(2);
        toLabelCell.setCellValue(translationService.translate("cmmsMachineParts.timeUsageReport.report.to", locale));
        toLabelCell.setCellStyle(style);
        if (filters.containsKey("toDate")) {
            HSSFCell toCell = datesRow.createCell(3);
            toCell.setCellValue(getDateOnly((Date) filters.get("toDate")));
        }

        HSSFRow authorRow = sheet.createRow(2);
        HSSFCell authorLabelCell = authorRow.createCell(0);
        authorLabelCell.setCellValue(translationService.translate("cmmsMachineParts.timeUsageReport.report.generatedBy", locale));
        authorLabelCell.setCellStyle(style);
        HSSFCell authorCell = authorRow.createCell(1);
        authorCell.setCellValue(getUserString());
    }

    private String getUserString() {
        Entity user = dataDefinitionService.get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER)
                .get(securityService.getCurrentUserId());
        return user.getStringField("firstName") + " " + user.getStringField("lastName") +
                " " + getDateValue(new Date());
    }

    private void fillHeaderRow(final HSSFWorkbook workbook, final HSSFSheet sheet, final Locale locale) {
        HSSFRow headerLine = sheet.createRow(4);
        headerLine.setHeight((short) 800);
        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBold(false);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        int colNumber = 0;
        for (String column : TimeUsageXlsConstants.ALL_COLUMNS) {
            HSSFCell headerCell = headerLine.createCell(colNumber);
            headerCell.setCellValue(translationService.translate(column, locale));
            headerCell.setCellStyle(style);
            colNumber++;
        }
    }

    private int fillTimeUsageRows(final HSSFWorkbook workbook, final HSSFSheet sheet, final TimeUsageGroupDTO timeUsage,
            int rowCounter, final Locale locale) {
        Font font = workbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBold(false);

        int usagesCounter = 0;

        for (TimeUsageDTO usage : timeUsage.getTimeUsages()) {
            HSSFRow usageRow = sheet.createRow(rowCounter + usagesCounter);
            boolean isFirst = usagesCounter == 0;
            HSSFCellStyle style = getLeftAlignedStyle(isFirst, usage.getEventType(), timeUsage.getDurationSum());
            HSSFCellStyle styleRight = getRightAlignedStyle(isFirst, usage.getEventType(), timeUsage.getDurationSum());
            addNewRow(usageRow, usage, locale, style, styleRight);
            if (isFirst) {
                addNewCell(usageRow, timeUsage.getDurationSum().toString(), 10, styleRight, true);
                addNewCell(usageRow, timeUsage.getRegisteredTimeSum().toString(), 11, styleRight, true);
            } else {
                addNewCell(usageRow, "", 10, styleRight, false);
                addNewCell(usageRow, "", 11, styleRight, false);
            }
            ++usagesCounter;
        }

        return rowCounter + usagesCounter;
    }

    private void addNewRow(HSSFRow usageRow, TimeUsageDTO timeUsage, Locale locale, HSSFCellStyle style,
            HSSFCellStyle styleAlignRight) {
        addNewCell(usageRow, timeUsage.getWorker(), 0, style, false);
        addNewCell(usageRow, getDateOnly(timeUsage.getStartDate()), 1, styleAlignRight, false);
        addNewCell(usageRow, timeUsage.getNumber(), 2, style, false);
        addNewCell(usageRow, translationService.translate(timeUsage.getType(), locale), 3, style, false);
        addNewCell(usageRow, translationService.translate(timeUsage.getState(), locale), 4, style, false);
        addNewCell(usageRow, timeUsage.getObject(), 5, style, false);
        addNewCell(usageRow, timeUsage.getParts(), 6, style, false);
        addNewCell(usageRow, timeUsage.getDescription(), 7, style, false);
        addNewCell(usageRow, timeUsage.getDuration().toString(), 8, styleAlignRight, true);
        addNewCell(usageRow, timeUsage.getRegisteredTime().toString(), 9, styleAlignRight, true);
    }

    private void addNewCell(HSSFRow row, String value, int column, HSSFCellStyle style, boolean numeric) {
        HSSFCell cell = row.createCell(column);
        cell.setCellStyle(style);
        if(numeric){
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue(Integer.parseInt(value));
        } else {
            cell.setCellValue(value);
        }


    }

    private HSSFCellStyle getStyle(boolean isFirst, boolean isLeft, String eventType, Long durationSum) {
        reportStyleFactory.setFirst(isFirst);
        if (isLeft) {
            reportStyleFactory.setLeftAligned();
        } else {
            reportStyleFactory.setRightAligned();
        }
        if ("maintenance".equals(eventType)) {
            if (420 <= durationSum && durationSum <= 480) {
                reportStyleFactory.setGreen();
            } else {
                reportStyleFactory.setRed();
            }
        } else {
            reportStyleFactory.setWhite();
        }
        return reportStyleFactory.getStyle();
    }

    private HSSFCellStyle getRightAlignedStyle(boolean isFirst, String eventType, Long durationSum) {
        return getStyle(isFirst, false, eventType, durationSum);
    }

    private HSSFCellStyle getLeftAlignedStyle(boolean isFirst, String eventType, Long durationSum) {
        return getStyle(isFirst, true, eventType, durationSum);
    }

    private String getDateValue(Date date) {
        if (date == null) {
            return "";
        }
        return DateUtils.toDateTimeString(date);
    }

    private String getDateOnly(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat(DateUtils.L_DATE_FORMAT);

        return df.format(date);
    }
}
