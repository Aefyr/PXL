package com.af.pxl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Float4;
import android.support.v8.renderscript.Int3;
import android.support.v8.renderscript.RenderScript;

import com.af.pxl.common.Vector2;

import java.util.HashSet;


/**
 * Created by Aefyr on 11.10.2017.
 */

public class ColorSwapperH {

    //Common
    private Context c;
    private int mode;
    private int oldColor = Color.RED;
    private int newColor = Color.WHITE;
    private Bitmap b;
    private boolean accelerationEnabled;

    //Accelerated swapTo resources
    private RenderScript rs;
    private ScriptC_color_swapper fillScript;
    private Allocation in;
    private Allocation out;

    //Slow swapTo resources
    private HashSet<Vector2> pixelsToSwap;

    public ColorSwapperH(Context context, Bitmap bitmap, int fromColor, int toColor){
        this.mode = mode;
        oldColor = fromColor;
        newColor = toColor;
        c = context;
        b = bitmap;
        accelerationEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("hardware_accelerated", true);
        initialize();
    }

    public void setAccelerationEnabled(boolean enabled){
        destroy();
        this.accelerationEnabled = enabled;
        initialize();
    }

    public void swapTo(int toColor){
        newColor = toColor;

        if(accelerationEnabled) {
            updateAcceleratedSwapResources();
            acceleratedSwap();
        }else {
            updateSlowSwapResources();
            slowSwap();
        }
    }

    public void destroy(){
        if(accelerationEnabled) {
            rs.destroy();
            fillScript.destroy();
            in.destroy();
            out.destroy();
        }
    }

    public long getDeltaTime(){
        long start = System.currentTimeMillis();
        swapTo(newColor);
        return System.currentTimeMillis()-start;
    }

    private void initialize(){
        if(accelerationEnabled)
            initializeAcceleratedSwapResources();
        else
            initializeSlowSwapResources();
    }

    private void initializeAcceleratedSwapResources(){
        rs = RenderScript.create(c);
        fillScript = new ScriptC_color_swapper(rs);

        in = Allocation.createFromBitmap(rs, b);
        out = Allocation.createTyped(rs, in.getType());

        fillScript.set_oldValue(new Int3(Color.red(oldColor), Color.green(oldColor), Color.blue(oldColor)));

        updateAcceleratedSwapResources();
    }

    private void updateAcceleratedSwapResources(){
        float r = (float) Color.red(newColor)/255f;
        float g = (float) Color.green(newColor)/255f;
        float b = (float) Color.blue(newColor)/255f;
        fillScript.set_newValue(new Float4(r, g, b, 1f));

        fillScript.invoke_packColor();
    }

    private void acceleratedSwap(){
        fillScript.forEach_swap(in, out);
        out.copyTo(b);
        oldColor = newColor;
        fillScript.set_oldValue(new Int3(Color.red(oldColor), Color.green(oldColor), Color.blue(oldColor)));
    }

    private void initializeSlowSwapResources(){
        pixelsToSwap = new HashSet<>();

        for(int x = 0; x<b.getWidth(); x++){
            for(int y = 0; y<b.getHeight(); y++){
                if(b.getPixel(x, y) == oldColor)
                    pixelsToSwap.add(new Vector2(x, y));
            }
        }
    }

    private void updateSlowSwapResources(){

    }

    private void slowSwap(){
        for(Vector2 pixel: pixelsToSwap){
            b.setPixel(pixel.x, pixel.y, newColor);
        }
    }

}
