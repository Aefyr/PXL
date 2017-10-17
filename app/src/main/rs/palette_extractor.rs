#pragma version(1)
#pragma rs java_package_name(com.af.pxl)
#pragma rs_fp_relaxed

static float3 rawPalette[16];
int paletteFull = 0;

int palette[16];

static float colorsDifference(float3 color1, float3 color2){
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

uchar4 __attribute__((kernel)) extract(uchar4 in){
    float4 f4 = rsUnpackColor8888(in);
    float3 color = {f4.r, f4.g, f4.b};

    if(paletteFull < 16){
        rawPalette[paletteFull] = color;
        paletteFull++;
        return in;
    }

    int leastDiff = 195075;
    int leastDiffIndex = 0;

    for(int i=0;i<16;i++){
        float diff = colorsDifference(rawPalette[i], color);
        if(diff<leastDiff){
            leastDiff = diff;
            leastDiffIndex = i;
        }
    }

    rawPalette[leastDiffIndex] = colorsMix(rawPalette[leastDiffIndex], color);

    return in;
}

void packPalette(){
    for(int i=0;i<16;i++){
        palette[i] = rgbColorToColorInt(rawPalette[i]);
    }
}