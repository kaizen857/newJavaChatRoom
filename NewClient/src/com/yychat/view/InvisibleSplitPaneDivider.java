package com.yychat.view;

import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

// Custom Divider that paints nothing
class InvisibleSplitPaneDivider extends BasicSplitPaneDivider {
    public InvisibleSplitPaneDivider(BasicSplitPaneUI ui) {
        super(ui);
        setBorder(null);
    }

    @Override
    public void paint(Graphics g) {
        // 不绘制
    }
}


