package com.af.pxl.tools;

import com.af.pxl.AdaptivePixelSurfaceH;
import com.af.pxl.R;

/**
 * Created by Aefyr on 05.08.2017.
 */

public class ToolPreview {

    public static int getIconForTool(AdaptivePixelSurfaceH.Tool tool){
        switch (tool){
            case ERASER:
                return R.drawable.eraser;
            case PENCIL:
                return R.drawable.pencil;
            case SELECTOR:
                return R.drawable.selection;
            case COLOR_PICK:
                return R.drawable.colorpick;
            case COLOR_SWAP:
                return R.drawable.colorswap;
            case FLOOD_FILL:
                return R.drawable.fill;
            case MULTISHAPE:
                return R.drawable.shapes;
        }

        return R.drawable.pencil;
    }
}
