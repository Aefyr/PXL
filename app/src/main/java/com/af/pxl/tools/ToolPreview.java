package com.af.pxl.tools;

import com.af.pxl.AdaptivePixelSurfaceH;

/**
 * Created by Aefyr on 05.08.2017.
 */

public class ToolPreview {
    int toolIconId;
    AdaptivePixelSurfaceH.Tool tool;

    public ToolPreview(int toolIconRes, AdaptivePixelSurfaceH.Tool tool) {
        this.toolIconId = toolIconRes;
        this.tool = tool;
    }
}
