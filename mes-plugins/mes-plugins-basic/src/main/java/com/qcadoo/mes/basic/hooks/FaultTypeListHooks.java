package com.qcadoo.mes.basic.hooks;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaultTypeListHooks {



    public void onBeforeRender(final ViewDefinitionState view) {
        disableActionsWhenDefault(view);
    }

    private void disableActionsWhenDefault(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup actions = window.getRibbon().getGroupByName("actions");

        RibbonActionItem copyButton = actions.getItemByName("copy");
        RibbonActionItem deleteButton = actions.getItemByName("delete");

        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        List<Entity> selectedFaults = grid.getSelectedEntities();

        for (Entity selectedFault : selectedFaults) {
            if (selectedFault.getBooleanField("isDefault")) {
                copyButton.setEnabled(false);
                deleteButton.setEnabled(false);
                copyButton.requestUpdate(true);
                deleteButton.requestUpdate(true);
                return;
            }
        }

        boolean enabled = !selectedFaults.isEmpty();

        copyButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        copyButton.requestUpdate(true);
        deleteButton.requestUpdate(true);
    }

}
