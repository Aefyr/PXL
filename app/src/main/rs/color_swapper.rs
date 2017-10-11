#pragma version(1)
#pragma rs java_package_name(com.af.pxl)
#pragma rs_fp_relaxed

int3 oldValue = {1, 1, 1};
float4 newValue = {1.f, 1.f, 1.f, 1.f};

uchar4 packedNewValue;

void packColor(){
packedNewValue = rsPackColorTo8888(newValue);
}

uchar4 __attribute__((kernel)) fill(uchar4 in){
    float4 f4 = rsUnpackColor8888(in);
    int r = f4.r * 255;
    int g = f4.g * 255;
    int b = f4.b * 255;
    if((int)(f4.r * 255) == oldValue.r && (int)(f4.g * 255) == oldValue.g && (int)(f4.b * 255) == oldValue.b){
	return packedNewValue;
	}else {
	return in;
	}
}