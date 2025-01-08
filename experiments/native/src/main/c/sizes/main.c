#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
#include <float.h>
#include "foo.h"

int main() {
	// char
	char Char = 'L';
	signed char sChar = 'm';
	unsigned char uChar = 'T';
	// int
	signed short int ssInt = -50;
	signed long int slInt = -2500;
	unsigned short int usInt = 20;
	unsigned long int ulInt = 400;
	signed int  sInt = -25;
	unsigned int uInt = 25;
	// floating-point
	float Float = 1.75;
	double Double = 100.225;
	long double lDouble = 0.2588;

	printf("TYPE: %-18s NAME: %-8s VALUE: %-10c MIN: %-22d MAX: %-22d BYTES: %-10lu\n", "char", "Char", Char, CHAR_MIN, CHAR_MAX, sizeof(Char));
	printf("TYPE: %-18s NAME: %-8s VALUE: %-10c MIN: %-22d MAX: %-22d BYTES: %-10lu\n", "signed char", "sChar", sChar, SCHAR_MIN, SCHAR_MAX, sizeof(sChar));
	printf("TYPE: %-18s NAME: %-8s VALUE: %-10c MIN: %-22d MAX: %-22d BYTES: %-10lu\n\n", "unsigned char", "uChar", uChar, 0, UCHAR_MAX, sizeof(uChar));

	printf("TYPE: %-18s NAME: %-8s VALUE: %-10d MIN: %-22d MAX: %-22d BYTES: %-10lu\n", "signed short int", "ssInt", ssInt, SHRT_MIN, SHRT_MAX, sizeof(ssInt));
	printf("TYPE: %-18s NAME: %-8s VALUE: %-10ld MIN: %-22ld MAX: %-22ld BYTES: %-10lu\n", "signed long int", "slInt", slInt, LONG_MIN, LONG_MAX, sizeof(slInt));
	printf("TYPE: %-18s NAME: %-8s VALUE: %-10u MIN: %-22u MAX: %-22u BYTES: %-10lu\n", "unsigned short int", "usInt", usInt, 0, USHRT_MAX, sizeof(usInt));
	printf("TYPE: %-18s NAME: %-8s VALUE: %-10lu MIN: %-22lu MAX: %-22lu BYTES: %-10lu\n", "unsigned long int", "ulInt", ulInt, 0, ULONG_MAX, sizeof(ulInt));
	printf("TYPE: %-18s NAME: %-8s VALUE: %-10d MIN: %-22d MAX: %-22d BYTES: %-10lu\n", "signed int", "sInt", sInt, INT_MIN, INT_MAX, sizeof(sInt));
	printf("TYPE: %-18s NAME: %-8s VALUE: %-10u MIN: %-22u MAX: %-22u BYTES: %-10lu\n\n", "unsigned int", "uInt", uInt, 0, UINT_MAX, sizeof(uInt));
	
	printf("TYPE: %-18s NAME: %-8s VALUE: %-10f MIN: %-22e MAX: %-22e BYTES: %-10lu\n", "float", "Float", Float, FLT_MIN, FLT_MAX, sizeof(Float));
	printf("TYPE: %-18s NAME: %-8s VALUE: %-10f MIN: %-22e MAX: %-22e BYTES: %-10lu\n", "double", "Double", Double, DBL_MIN, DBL_MAX, sizeof(Double));
	printf("TYPE: %-18s NAME: %-8s VALUE: %-10lf MIN: %-22e MAX: %-22e BYTES: %-10lu\n\n", "long double", "lDouble", lDouble, LDBL_MIN, LDBL_MAX, sizeof(lDouble));

    // custom types
    Point point = { 10.0, 20.0 };
    printf("TYPE: %-18s NAME: %-8s VALUE: (%f, %f) BYTES: %-10lu\n\n", "Point", "point", point.x, point.y, sizeof(point));

    // foos
    char* pChar = &Char;
    Point* pPoint = &point;
    printf("TYPE: %-18s NAME: %-8s VALUE: %-10p BYTES: %-10lu\n", "char*", "pChar", pChar, sizeof(pChar));
    printf("TYPE: %-18s NAME: %-8s VALUE: %-10p BYTES: %-10lu\n", "Point*", "pPoint", pPoint, sizeof(pPoint));

	return 0;
}