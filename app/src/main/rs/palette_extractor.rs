#pragma version(1)
#pragma rs java_package_name(com.af.pxl)
#pragma rs_fp_relaxed

volatile static float3 rawPalette[16];
volatile int paletteFull = 0;

rs_allocation palette;

static float colorsDifference(float3 color1, float3 color2){
    //return fabs(color2.r - color1.r) + fabs(color2.g - color1.g) + fabs(color2.b - color1.b);
    return pow(color2.r-color1.r, 2)+pow(color2.g-color1.g, 2)+pow(color2.b-color1.b, 2);
}

static float3 colorsMix(float3 color1, float3 color2){
    float3 mixedColor = {(color1.r+color2.r)/2.0f,(color1.g+color2.g)/2.0f, (color1.b+color2.b)/2.0f};
    return mixedColor;
}

static int rgbColorToColorInt(float3 color){
    int red = color.r*255;
    int green = color.g*255;
    int blue = color.b*255;
    return (0xFF << 24) | (red << 16) | (green << 8) | blue;
}

static bool alreadyContains(float3 color){
    for(int i=0; i<16; i++){
        if(rawPalette[i].r == color.r && rawPalette[i].g == color.g && rawPalette[i].b == color.b){
            return true;
        }
    }
    return false;
}

void RS_KERNEL extract(uchar4 in){
    float4 f4 = rsUnpackColor8888(in);
    float3 color = {f4.r, f4.g, f4.b};

    if(paletteFull < 16){
        if(!alreadyContains(color)){
            rawPalette[paletteFull] = color;
            rsAtomicInc(&paletteFull);
        }
        return;
    }

    float leastDiff = 195075;

    for(int i=0;i<16;i++){
        float diff = colorsDifference(rawPalette[i], color);
        if(diff<leastDiff){
            leastDiff = diff;
        }
    }

    int2 lowestColorsDiffIndicies = {-1, -1};
    for(int x=0;x<16;x++){
        for(int y=0;y<16;y++){
            if(y==x){
                continue;
            }
            float diff = colorsDifference(rawPalette[x], rawPalette[y]);
            if(diff<leastDiff){
                leastDiff = diff;
                lowestColorsDiffIndicies.x = x;
                lowestColorsDiffIndicies.y = y;
            }
        }
    }

    if(lowestColorsDiffIndicies.x != -1){
        rawPalette[lowestColorsDiffIndicies.x] = color;
    }


}

void packPalette(){
    for(int i=0;i<16;i++){
       rsSetElementAt_int(palette, rgbColorToColorInt(rawPalette[i]), i);
    }
}