/*
 * Shiying Zheng, CS 210
 * A floating point algebra simulator
 */
public class MyXfloat extends Xfloat {

  //set to true to display debug info for the xadd function
  boolean debug=false;
  //set to true to display debug info for the xmult function
  boolean debug2=false;

  public MyXfloat(){super();}
  public MyXfloat(float f){super(f);}
  public MyXfloat(byte sign, int exp, int man){super(sign, exp, man);}

  /**
  * Adds two floating point numbers
  * @return an Xfloat as the sum
  */
  public Xfloat xadd(Xfloat y) {
    byte new_sign;
    int new_exp=0;
    int new_man=0;

    //Special case for 0
    if (this.man==0 && this.exp ==0) return y;
    if (y.man==0 && y.exp == 0) return this;

    //Get the mantissas of the two numbers we are adding, with the implicit 1 in front
    int x_man=this.man | BMASK;
    int y_man=y.man | BMASK;

    if (debug){
      print_binary("The mantissa for x with implicit 1 is ", x_man);
      print_binary("The mantissa for y with implicit 1 is ", y_man);
    }

    int exp_diff=this.exp-y.exp; //find the difference of the exp's so we can shift things over

    //Shift the mantissa to get everything at the same exp
    if (exp_diff<0){
      //need to shift x's mantissa
      x_man=x_man>>>(0-exp_diff);
      new_exp=y.exp;
    }
    else if (exp_diff>0){
      //need to shift y's mantissa
      y_man=y_man>>>exp_diff;
      new_exp=this.exp;
    }
    else{
      new_exp=this.exp;
    }

    if (debug){
      print_binary("After shifting x's mantissa is ", x_man);
      print_binary("After shifting y's mantissa is ", y_man);
    }

    //Determining whether we add or subtract, and what the sign should be,
    //based on the man and sign of the two numbers
    if (x_man>y_man){
      if (debug) System.out.println("Case 1");

      int exp_diff=this.exp-y.exp; //find the difference of the exp's so we can shift things over
      new_sign=this.sign;

      if (y.sign==new_sign){
        new_man=x_man+y_man;
      }
      else{
        new_man=x_man-y_man;
      }

    }
    else if (x_man<y_man){
      if (debug) System.out.println("Case 2");

      new_sign=y.sign;

      if (this.sign==new_sign){
        new_man=x_man+y_man;
      }
      else{
        new_man=y_man-x_man;
      }

    }
    else{
      if (debug) System.out.println("Case 3");

      new_sign=y.sign;

      if (this.sign==new_sign){
        new_man=x_man+y_man;
      }
      else {
        new_man=0;
      }
    }

    if (new_man==0) return new MyXfloat((byte)0,0,0); //otherwise enter infinite loop below

    if (debug) print_binary("The exponent before shifting is ", new_exp);

    //Get rid of leading 0's
    while((new_man & 0x80000000)==0){
      new_man=new_man<<1;
      new_exp=new_exp-1;
    }
    new_man=new_man>>>8; //shift the mantissa so it starts at the 8th digit
    new_exp=new_exp+8; //add 8 back into exp that we subtracted when we were getting rid of leading 0's

    if (debug) print_binary("The mantissa after shifting is ", new_man);

    new_man=new_man & MMASK; //take out the implicit 1 in front

    return new MyXfloat((byte)new_sign,new_exp,new_man);
  }

  /**
  * Multiplies two floating point numbers together
  * @return an Xfloat as the product
  */
  public Xfloat xmult(Xfloat y) {
    byte new_sign;
    int new_exp=0;
    long new_man=0;

    //Handle the special case of 0
    if ((this.man==0 && this.exp==0) || (y.man==0 && y.exp==0)) return new MyXfloat((byte)0,0,0);

    //Figure out the sign of the product
    if (this.sign==y.sign) new_sign=0;
    else new_sign=1;

    //Putting the implicit 1 in front back into the mantissas
    long x_man=(long) (this.man | BMASK);
    long y_man=(long) (y.man | BMASK);

    //Get the number of digits after the decimal point
    int x_dec=decimal_length(x_man);
    int y_dec=decimal_length(y_man);

    if (debug2){
      print_binary("The mantissa for x with implicit 1 is ", x_man);
      print_binary("The mantissa for y with implicit 1 is ", y_man);
      System.out.println("Decimal length of x is " + x_dec);
      System.out.println("Decimal length of y is " + y_dec);
    }

    //Multiply the mantissas, calculate the exp
    new_man=x_man*y_man;
    new_exp=(this.exp-127)+y.exp;

    //Get the number of digits after the decimal point for the mantissa of the product
    //So we can adjust the exp
    int new_dec=decimal_length(new_man);

    if (debug2){
      System.out.println("Exp is currently " + (new_exp-127));
      System.out.println("Decimal length of x is " + new_dec);
    }

    if (new_man==0) return new MyXfloat((byte)0,0,0); //otherwise enter infinite loop below

    //Shift the mantissa over so it gets to the last 23 bits
    long extra_bits = 0xffffffffff000000L;
    while ((new_man & extra_bits) > 0){
      new_man=new_man>>>1;
    }

    if (debug2){
      print_binary("The mantissa is ", new_man);
    }

    //Adjust the exp
    new_exp=new_exp+(new_dec-x_dec-y_dec);

    //Take out implicit 1 in front
    new_man=new_man & MMASK;

    return new MyXfloat((byte)new_sign,new_exp, (int) new_man);
  }

  /**
  * Prints a number in binary representation
  */
  public static void print_binary(String msg, long x){
    System.out.print(msg);
    String s="";
    if (x==0) System.out.println(0);
    while(x!=0){
      if (x%2==1) s="1"+s;
      else s="0"+s;
      x=x/2;
    }
    System.out.println(s);
  }

  /**
  * Counts the number of digits after the decimal point in a floating number's binary representation
  * Assuming the number has only one digit before the decimal point
  * @return the number of digits after the decimal point
  */
  public static int decimal_length(long x){
    int[] ans = new int[64];
    int mask = 1;

    //Figure out the binary representation of the number
    for (int i=0;i<64;i++){
      if (x%2==0){
        ans[64-(i+1)]=0;
      }
      else{
        ans[64-(i+1)]=1;
      }
      x=x/2;
    }

    //Find the index of the first non-zero digit
    int min=0;
    for (int i=0 ; i<64 ; i++){
      if (ans[i]==1){
        min=i;
        break;
      }
    }

    //Find the index of the last non-zero digit
    int max=0;
    for (int i=63; i >=0 ; i--){
      if (ans[i]==1){
        max=i;
        break;
      }
    }

    return max-min; //The difference is the number of digits after the decimal point
  }

  public static void main(String arg[]) {
    if (arg.length < 2) return;
    float x = Float.valueOf(arg[0]).floatValue(),
          y = Float.valueOf(arg[1]).floatValue();
    Xfloat xf, yf, zf, wf;
    xf = new MyXfloat(x);
    yf = new MyXfloat(y);
    zf = xf.xmult(yf);
    wf = xf.xadd(yf);
    System.out.println("x:   "+xf+" "+x);
    System.out.println("y:   "+yf+" "+y);
    System.out.println("x*y: "+zf+" "+zf.toFloat());
    System.out.println("x+y: "+wf+" "+wf.toFloat());
  }
}
