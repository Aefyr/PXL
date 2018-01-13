#pragma version(1)
#pragma rs java_package_name(com.aefyr.pxl)
#pragma rs_fp_relaxed

rs_allocation palette;
int paletteSize = 16;

static int rgbaToInt(float4 rgba){
    int red = (int)(rgba.r * 255.0f);
    int green = (int)(rgba.g * 255.0f);
    int blue = (int)(rgba.b * 255.0f);
    return 0xff000000 | (red << 16) | (green << 8) | blue;
}

static float4 intToRgba(int colorInt){
    //This is so lidl
    float r = (((float)(colorInt & 0x00FF0000))/65536.0f)/255.0f;
    float g = (((float)(colorInt & 0x0000FF00))/256.0f)/255.0f;
    float b = ((float)(colorInt & 0x000000FF))/255.0f;

    float4 rgbaColor = {r,g,b,1};
    return rgbaColor;
}

static float dist(float4 color1, float4 color2){
    return sqrt(pow(color2.r*255.0f-color1.r*255.0f, 2.0f)+pow(color2.g*255.0f-color1.g*255.0f, 2.0f)+pow(color2.b*255.0f-color1.b*255.0f, 2.0f));
}

uchar4 RS_KERNEL posterize(uchar4 in){
    float4 f4Color = rsUnpackColor8888(in);

    int closestColor = -1;
    float minimalDiff = 255.0f * 255.0f * 255.0f;
    for(int i=0; i<paletteSize; i++){
        float difference = dist(f4Color, intToRgba(rsGetElementAt_int(palette, i)));

        if(difference<minimalDiff){
            closestColor = i;
            minimalDiff = difference;
        }
    }

    return rsPackColorTo8888(intToRgba(rsGetElementAt_int(palette, closestColor)));
}