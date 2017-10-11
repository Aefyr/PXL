package com.af.pxl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Float4;
import android.support.v8.renderscript.Int3;
import android.support.v8.renderscript.RenderScript;


/**
 * Created by Aefyr on 11.10.2017.
 */

public class ColorSwapperH {
    private Context c;
    private RenderScript rs;
    private ScriptC_color_swapper fillScript;

    public ColorSwapperH(Context c){
        this.c = c;
        createRS();
    }

    private void createRS(){
        rs = RenderScript.create(c);
        fillScript = new ScriptC_color_swapper(rs);
    }

    public void swap(Bitmap bitmap, int oldColor, int newColor){

        fillScript.set_oldValue(new Int3(Color.red(oldColor), Color.green(oldColor), Color.blue(oldColor)));

        float r = (float) Color.red(newColor)/255f;
        float g = (float) Color.green(newColor)/255f;
        float b = (float) Color.blue(newColor)/255f;
        fillScript.set_newValue(new Float4(r, g, b, 1f));

        Allocation in = Allocation.createFromBitmap(rs, bitmap);
        Allocation out = Allocation.createTyped(rs, in.getType());

        fillScript.invoke_packColor();
        fillScript.forEach_fill(in, out);

        out.copyTo(bitmap);

        in.destroy();
        out.destroy();
    }

}
