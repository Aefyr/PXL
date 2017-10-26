package com.af.pxl.TCA;

import android.content.Context;

import com.af.pxl.AdaptivePixelSurfaceH;
import com.af.pxl.R;
import com.af.pxl.util.Utils;

/**
 * Created by Aefyr on 14.10.2017.
 */

public class ToolsTutorials {
    private Context c;

    public ToolsTutorials(Context c){
        this.c = c;
    }

    public void showTutorialForTool(AdaptivePixelSurfaceH.Tool tool){
        switch (tool) {
            case PENCIL:
                showDialog(c.getText(R.string.pencil), c.getText(R.string.tt_pencil));
                break;
            case FLOOD_FILL:
                showDialog(c.getText(R.string.fill), c.getText(R.string.tt_fill));
                break;
            case COLOR_PICK:
                showDialog(c.getText(R.string.colorpick), c.getText(R.string.tt_color_pick));
                break;
            case COLOR_SWAP:
                showDialog(c.getText(R.string.color_swap), c.getText(R.string.tt_color_swap));
                break;
            case ERASER:
                showDialog(c.getText(R.string.eraser), c.getText(R.string.tt_eraser));
                break;
            case MULTISHAPE:
                showDialog(c.getText(R.string.multishape), c.getText(R.string.tt_multishape));
                break;
            case SELECTOR:
                showDialog(c.getText(R.string.selector), c.getText(R.string.tt_selector));
                break;
        }
    }

    private void showDialog(CharSequence title, CharSequence message){
        Utils.easyAlert(c, title, message).show();
    }
}
