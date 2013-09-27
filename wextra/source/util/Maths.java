/*****************************************************************************

 *                                Waba Extras

 *

 * Version History

 * Date                Version  Programmer

 * ----------  -------  -------  ------------------------------------------

 * 23/04/1999  New      1.0.0    Joe McDaniel

 * Class created

 *

 * 02/06/1999  New      1.1.0    Rob Nielsen

 * Changed the exp() function due to accuracy problems and brought in some

 * methods from the old Maths class.

 *

 * 20/08/1999  New      1.1.1    Rob Nielsen

 * Modified exp() to use a lookup table for factorials as per suggestion

 * by Steven Rohall

 *

 * 27/03/1999  New      1.2.0    Rob Nielsen

 * Modified asin(), atan(), log() and sin() to use Horners rule to reduce

 * equations as per suggestion from Jody Nickel.

 *

 ****************************************************************************/

package extra.util;



/**

 * The class <code>Math</code> contains methods for performing basic

 * numeric operations such as the elementary exponential, logarithm,

 * square root, and trigonometric functions. One big change is that (in

 * general) all methods use and return <code>float</code>s instead of

 * <code>double</code>s!

 * <p>

 * Math provides approximately 5 decimal place accuracy

 * versions of the methods provided by java.math.Math.

 * The approximations are based on those published in

 * "Approximations for Digital Computers" by Cecil Hastings, Jr.,

 *  Princeton University Press, 1955.

 * <p>

 * I chose not to use the implementations noted below from netlib

 * (not at netlib.att.com!) as they provide more accuracy (and would

 * be slower) than seemed needed for a hand-held device. I have left in

 * the reference for completeness.  I realized after a review of Float that

 * one can get/set the bits for a floating value using intBitsToFloat and

 * floatToIntBits. Therefore, it would be possible to implement the fdlibm

 * versions of the algorithms where more accuracy and robustness is needed.

 * <p>

 * To help ensure portability of Java programs, the definitions of

 * many of the numeric functions in this package require that they

 * produce the same results as certain published algorithms. These

 * algorithms are available from the well-known network library

 * <code>netlib</code> as the package "Freely Distributable

 * Math Library" (<code>fdlibm</code>). These algorithms, which

 * are written in the C programming language, are then to be

 * understood as executed with all floating-point operations

 * following the rules of Java floating-point arithmetic.

 * <p>

 * The network library may be found on the World Wide Web at

 * <ul><code>

 *   http://netlib.att.com/

 * </code></ul>

 * <p>

 * then perform a keyword search for "<code>fdlibm</code>".

 * <p>

 * The Java math library is defined with respect to the version of

 * <code>fdlibm</code> dated January 4, 1995. Where

 * <code>fdlibm</code> provides more than one definition for a

 * function (such as <code>acos</code>), use the "IEEE 754 core

 * function" version (residing in a file whose name begins with

 * the letter <code>e</code>).

 * <p>

 *

 * @author     <A HREF="mailto:jrmcdaniel@home.com">Joe McDaniel</A>,

 * @version   1.1.1 20 August 1999

 */

 /*

  * No claims as to accuracy, robustness, usablility, safety, or anything

  * else. Use as you see fit at your own risk.

  */

public class Maths

{

  // A lookup table for factorials from 0! to 16!. (used in exp)

  private static float[] factorial = { 1f, 1f, 2f, 6f, 24f, 120f, 720f, 5040f, 40320f, 362880f, 3628800f, 39916800f, 479001600f, 6227020800f, 87178291200f, 1307674368000f, 20922789888000f};



  /**

   * The <code>double</code> value that is closer than any other to

   * <code>e</code>, the base of the natural logarithms.

   *

   * @since   JDK1.0

   */

  public static final float E = 2.7182818284590452354f;



  /**

   * The <code>double</code> value that is closer than any other to

   * <i>pi</i>, the ratio of the circumference of a circle to its diameter.

   *

   * @since   JDK1.0

   */

  public static final float PI = 3.14159265358979323846f;



  //public static float NaN = Float.intBitsToFloat(0xff8fffff);



  private Maths(){}; // null constructor



  /**

   * Returns the absolute value of a <code>float</code> value.

   * If the argument is not negative, the argument is returned.

   * If the argument is negative, the negation of the argument is returned.

   *

   * @param a a <code>float</code> value.

   * @return the absolute value of the argument.

   */

  public static float abs(float a)

  {

    return (a < 0) ? -a : a;

  }



  /**

   * Returns the absolute value of a <code>int</code> value.

   * If the argument is not negative, the argument is returned.

   * If the argument is negative, the negation of the argument is returned.

   *

   * @param a a <code>int</code> value.

   * @return the absolute value of the argument.

   */

  public static int abs(int a)

  {

    return (a < 0) ? -a : a;

  }



  /**

   * Returns the sign of a <code>float</code> value.

   * If the argument is not negative, 1 is returned.

   * If the argument is negative, -1 is returned.

   *

   * @param a a <code>float</code> value.

   * @return the sign of the argument.

   */

  private static int sign(float x)

  {

    return (x >= 0) ? +1 : -1;

  }



  /**

   * Rounds the given <code>float</code> to the nearest whole number.

   * @param x the number to round

   * @returns the rounded number

   */

  public static int round(float x)

  {

    return (int)(x+0.5f);

  }



  /**

   * Returns the minimum of the two given <code>int</code> values.

   * @returns the minimim

   */

  public static int min(int a,int b)

  {

    return (a>b)?b:a;

  }



  /**

   * Returns the maximum of the two given <code>int</code> values.

   * @returns the maximum

   */

  public static int max(int a,int b)

  {

    return (a>b)?a:b;

  }



  /**

   * Returns the minimum of the two given <code>float</code> values.

   * @returns the minimim

   */

  public static float min(float a,float b)

  {

    return (a>b)?b:a;

  }



  /**

   * Returns the maximum of the two given <code>float</code> values.

   * @returns the maximum

   */

  public static float max(float a,float b)

  {

    return (a>b)?a:b;

  }



  /**

   * Returns the arc sine of an angle, in the range of -<i>pi</i>/2 through

   * <i>pi</i>/2.

   *

   * @param   a   an angle, in radians.

   * @return  the arc sine of the argument.

   */

  public static float asin(float x)

  {

    // normalize x

    int sign = (x > 0) ? +1 : -1;

      x = x % 1f;

    if (x < 0f)

    {

      x += 1f;

    }

    float phi=(((0.00864884f*x-0.03575663f)*x+0.08466649f)*x-0.21412453f)*x+1.57078786f;

    return sign * (float)(PI/2f - sqrt(1f - x) * phi);

  }



  /**

   * Returns the arc cosine of an angle, in the range of 0.0 through

   * <i>pi</i>.

   *

   * @param   a   an angle, in radians.

   * @return  the arc cosine of the argument.

   */

  public static float acos(float x)

  {

    return asin(sqrt(1f - x * x));

  }



  /**

   * Returns the trigonometric tangent of an angle.

   *

   * @param   a   an angle, in radians.

   * @return  the tangent of the argument.

   */

  public static float tan(float x)

  {

    return sin(x)/cos(x);

  }



  /**

   * Returns the arc tangent of an angle, in the range of -<i>pi</i>/2

   * through <i>pi</i>/2.

   *

   * @param   a   an angle, in radians.

   * @return  the arc tangent of the argument.

   */

  public static float atan(float a)

  {

    int sign = +1;



    if(a < 0f)

    {

      sign = -1;

      a = -a;

    }



    float x = (a - 1f)/(a + 1f);

    float x2=x*x;

    return sign*(((((0.0208351f*x2-0.0851330f)*x2+0.1801410f)*x2-0.3302995f)*x2+0.9998660f)*x+PI/4f);

  }



  /**

   * Converts rectangular coordinates (<code>b</code>,&nbsp;<code>a</code>)

   * to polar (r,&nbsp;<i>theta</i>).

   * This method computes the phase <i>theta</i> by computing an arc tangent

   * of <code>b/a</code> in the range of -<i>pi</i> to <i>pi</i>.

   *

   * @param   a   a <code>float</code> value.

   * @param   b   a <code>float</code> value.

   * @return  the <i>theta</i> component of the point

   *          (<i>r</i>,&nbsp;<i>theta</i>)

   *          in polar coordinates that corresponds to the point

   *          (<i>b</i>,&nbsp;<i>a</i>) in Cartesian coordinates.

   */

  public static float atan2(float y, float x)

  {

    float atan2 = 0;

    if( x > 0f)

    {

      atan2 = atan(y/x);

    }

    else if (x < 0f)

    {

      atan2 = sign(y)* (float)(PI - atan(abs(y/x)));

    }

    else if (x == 0 && y == 0)

    {

      atan2 = 0f; //NaN;

    }

    else // x = 0, y != 0

    {

      atan2 = sign(y) * (float)(PI / 2f);

    }

    return atan2;

  }



  /**

   * Returns the square root of a <code>float</code> value.

   *

   * @param   a   a <code>float</code> value.

   * <!--@return  the value of &radic;&nbsp;<code>a</code>.-->

   * @return  the square root of <code>a</code>.

   *          If the argument is NaN or less than zero, the result is NaN.

   * @since   JDK1.0

   */

  public static float sqrt(float a)

  {

    float x1, x0, diff;

    int sign = +1;

      if (a < 0f)

        return 0f; //NaN;



    x0 = .5f * a;



      do

    {

      x1 = .5f * (x0 + a/x0);

      diff = x1 - x0;

      if (diff < 0) diff = -diff;

          x0 = x1;

    } while (diff > .000001f);

    return x1;

  }





  /**

   * Returns the exponential number <i>e</i> (i.e., 2.718...) raised to

   * the power of a <code>double</code> value.

   *

   * @param   a   a <code>double</code> value.

   * @return  the value <i>e</i><sup>a</sup>, where <i>e</i> is the base of

   *          the natural logarithms.

   */

  public static float exp(float x)

  {

    // Calculate the first several terms of the Taylor expansion for e^x.

    // e^x = sum(n=0 to infinity)(x^n)/n!

    float numerator= 1.0f;

    float result = 0.0f;

    for(int n=0; n<factorial.length; n++)

    {

      result += numerator / factorial[n];

      numerator *= x;

    }

    return(result);

  }



  /**

   * Returns of value of the first argument raised to the power of the

   * second argument.

   * <p>

   * @param   a   a <code>float</code> value.

   * @param   b   a <code>float</code> value.

   * @return  the value <code>a<sup>b</sup></code>.

   */

  public static float pow(float a, float b)

  {

      // do this with logs

      float temp = log(a);

      temp *= b;

      return exp(log(a) * b);

  }



  /**

   * Returns the natural logarithm (base <i>e</i>) of a <code>float</code>

   * value.

   *

   * @param   a   a number greater than <code>0.0</code>.

   * @return  the value ln&nbsp;<code>a</code>, the natural logarithm of

   *          <code>a</code>.

   */

  public static float log(float x)

  {

    // compute offset



    int radix = 0;



    if (x == 0f)

    {

      return 0f; //NaN;

    }

    else if (x < .1f)

    {

      while(x < .1f)

      {

        radix--;

        x *= 10f;

      }

    }

    else

    {

      while (x > 10f)

      {

        radix++;

        x /= 10f;

      }

    }

    float r = (x - 3.1622776f)/(x + 3.1622776f);

    float r2=r*r;

    float mantissa = (((.21139497f*r2 +.15361371f)*r2+.29115068f)*r2+.86855434f)*r+.5f;

    return (float)((radix + mantissa) / 0.43429448190f);

  }



  /**

   * Returns the trigonometric sine of an angle.

   *

   * @param   a   an angle, in radians.

   * @return  the sine of the argument.

   */

  public static float sin(float radians)

  {

    float deg = (float)(radians * 180f/PI);

    deg = deg % 360f;

    if (deg < 0f) deg = 360f + deg;

    float tmpdeg = deg;

    if (deg >= 0f && deg < 90f)

      tmpdeg = deg;

    else if (deg >= 90f && deg < 180f)

      tmpdeg = 180f - deg;

    else if (deg >= 180f && deg < 270f)

      tmpdeg = 180f - deg;

    else if (deg >= 270f && deg < 360f)

      tmpdeg = -(360f - deg);

    float x = tmpdeg /90f;

    float x2=x*x;

    return (((-0.004362476f*x2+0.079487663f)*x2-0.645920978f)*x2+1.570794852f)*x;

  }



  /**

   * Returns the trigonometric cosine of an angle.

   *

   * @param   a   an angle, in radians.

   * @return  the cosine of the argument.

   */

  static float cos(float radians)

  {

    float cos = asin((float)(PI/2 - radians));

    return cos;

  }



  public static void main(String args[])

  {

    float diff=0f;

    for(float f=0f;f<5f;f+=0.01f)

    {

			asin(f);

		}

		/*

      float fexp=exp(f);

      float dexp=(float)Math.exp((double)f);

      diff+=Math.abs(dexp-fexp);

      System.out.println(fexp+":"+dexp);

    }

    System.out.println(">>"+diff+":"+Math.exp(1.0));

    */

  }

}