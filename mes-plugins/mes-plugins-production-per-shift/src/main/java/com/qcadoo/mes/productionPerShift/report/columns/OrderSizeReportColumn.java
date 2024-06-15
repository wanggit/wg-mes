package com.qcadoo.mes.productionPerShift.report.columns;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.report.PPSReportXlsHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Component("orderSizeReportColumn")
public class OrderSizeReportColumn extends AbstractReportColumn {

    private PPSReportXlsHelper ppsReportXlsHelper;

    private NumberService numberService;

    @Autowired
    public OrderSizeReportColumn(final TranslationService translationService, final PPSReportXlsHelper ppsReportXlsHelper,
            final NumberService numberService) {
        super(translationService);
        this.ppsReportXlsHelper = ppsReportXlsHelper;
        this.numberService = numberService;
    }

    @Override
    public String getIdentifier() {
        return "orderSize";
    }

    @Override
    public Object getValue(final Entity productionPerShift) {
        return ppsReportXlsHelper.getOrder(productionPerShift).getDecimalField(OrderFields.PLANNED_QUANTITY).setScale(5)
                .doubleValue();
    }

    @Override
    public Object getFirstRowValue(final Entity productionPerShift) {
        return getValue(productionPerShift);
    }

    @Override
    public int getColumnWidth() {
        return 7 * 256;
    }

}
