#pragma version(1)
#pragma rs java_package_name(com.aefyr.pxl)
#pragma rs_fp_relaxed

int3 oldValue = {1, 1, 1};
float4 newValue = {1.f, 1.f, 1.f, 1.f};

uchar4 packedNewValue;

void packColor(){
packedNewValue = rsPackColorTo8888(newValue);
}

uchar4 __attribute__((kernel)) swap(uchar4 in){

    float4 f4 = rsUnpackColor8888(in);
    if((int)(f4.r * 255) == oldValue.r && (int)(f4.g * 255) == oldValue.g && (int)(f4.b * 255) == oldValue.b){
	return packedNewValue;
	}else {
	return in;
	}
}