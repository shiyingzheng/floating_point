#include <stdio.h>

#define SMASK ((unsigned)0x80000000) //10000...
#define EMASK ((unsigned)0x7f800000) //011111111000...
#define MMASK ((unsigned)0x007fffff) //00000000011111111111111111111111
#define BMASK ((unsigned)0x00800000) //00000000100...

/* The following struct defines the XFLOAT type */
typedef struct {
  unsigned char sign;
  unsigned char exp;
  unsigned long man;
} XFLOAT;

/* The following union is used in conversions between float and XFLOAT */
typedef union {
  float f;
  long l;
} XFCONV;

float xfltofl(XFLOAT*);
void fltoxfl(float, XFLOAT*);
void xmult(XFLOAT*, XFLOAT*, XFLOAT*);
void xadd(XFLOAT*, XFLOAT*, XFLOAT*);
void printline(char*, XFLOAT*x);

int main(int argc, char *argv[])
{
  XFLOAT xf, yf, zf, wf;
  float x, y;
  XFCONV cv;

  if (argc < 3) exit(1);
  sscanf(argv[1], "%f", &x);
  sscanf(argv[2], "%f", &y);
  fltoxfl(x, &xf);
  fltoxfl(y, &yf);
  xmult(&xf, &yf, &zf);
  xadd(&xf, &yf, &wf);
  printline("x:", &xf);
  printline("y:", &yf);
  printline("x*y:", &zf);
  printline("x+y:", &wf);
}

void xmult(XFLOAT* x, XFLOAT* y, XFLOAT* z)
{
  z->sign = z->exp = z->man = 0; 
}

void xadd(XFLOAT* x, XFLOAT* y, XFLOAT* z)
{
  z->sign = z->exp = z->man = 0; 
}

/* convert xfloat to float */
float xfltofl(XFLOAT* xf)
{
  XFCONV xu;

  xu.l = (((unsigned long)xf->sign) << 31) | 
         (((unsigned long)xf->exp) << 23) |
         (unsigned long)xf->man;
  return xu.f;
}

/* convert float to XFLOAT */
void fltoxfl(float x, XFLOAT *xf)
{
  XFCONV xu;

  xu.f = x;
  xf->sign = (unsigned char) ((xu.l & SMASK) >> 31);
  xf->exp = (unsigned char) ((xu.l & EMASK) >> 23);
  xf->man = (unsigned long) (xu.l & MMASK);
}

/* Converts x to binary and copies its low order bits to d */
void toBinary(unsigned long x, char*d, int len){
  int bit;
  if(len>0){
    d = d+len-1;
    while(len>0){
      bit = x & 1;
      *d-- = '0'+bit;
      x>>=1;
      --len;
    }
  }
}

/* Displays x (with given prefix) in hex, binary, and float formats */
void printline(char*prefix, XFLOAT*x){
  XFCONV cv;
  char bin[] = "(0 00000000 00000000000000000000000)";
  cv.f = xfltofl(x);
  toBinary(x->sign, bin+1, 1);
  toBinary(x->exp, bin+3, 8);
  toBinary(x->man, bin+12, 23);
  printf("%-4s %08x %s s: %d e: %02x m: %06x  %f\n", 
       prefix, cv.l, bin, x->sign, x->exp, x->man, cv.f);
}

