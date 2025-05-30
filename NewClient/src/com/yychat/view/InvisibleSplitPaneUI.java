package com.yychat.view;

import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

// Custom UI that uses the InvisibleSplitPaneDivider
class InvisibleSplitPaneUI extends BasicSplitPaneUI {
    @Override
    public BasicSplitPaneDivider createDefaultDivider() {
        return new InvisibleSplitPaneDivider(this);
    }
}
