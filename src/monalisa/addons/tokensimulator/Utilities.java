/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator;

import java.util.concurrent.TimeUnit;
import static monalisa.addons.tokensimulator.TokenSimulator.strings;

/**
 *
 * @author Pavel Balazki.
 */
public class Utilities {
    //BEGIN VARIABLES DECLARATION
    /**
     * Factorial values 1! - 20!
     */
    private static final long[] FACTORIAL = {
        1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800,
        39916800L, 479001600L, 6227020800L, 87178291200L, 1307674368000L,
        20922789888000L, 355687428096000L, 6402373705728000L, 121645100408832000L, 2432902008176640000L
    };
    //END VARIABLES DECLARATION
    
    //BEGIN INNER CLASSES
    public static class FactorialTooBigException extends Exception{
        public FactorialTooBigException(){
            super(strings.get("TSFactorialTooBigException"));
        }
    }
    //END INNER CLASSES
    
    //BEGIN CONSTRUCTORS
    private Utilities(){}
    //END CONSTRUCTORS    
    
    /**
    * Computes natural logarithm of gamma function of n. Used for calculating factorials, since
    * n! = gamma(n+1).
    * See William et al. (1999) Numerical Recipes in C, Second Edition. Cambridge University Press, 6.1: 213-214.
    * @param n
    * @return 
    */
    public static double gammaln(double n){
        double x, y, tmp, ser;
        double[] cof = {76.18009172947146, -86.50532032941677, 24.01409824083091,
        -1.231739572450155, 0.1208650973866179E-2, 0.5395239384953E-5};
        int j;

        y = n;
        x = n;
        tmp = x+5.5;
        tmp -= (x+0.5)*Math.log(tmp);
        ser = 1.000000000190015;
        for (j = 0; j <= 5; j++){
            ser += cof[j]/++y;
        }
        return -tmp+Math.log(2.5066282746310005*ser/x);
    }

    /**
    * Compute the factorial of given integer. Call this function only for i &lq= 20. Otherwise, use the natural logarithm of i! by calling 
    * factorialln(i). This is necessary because factorials of i greater than 20 are too big for a Long.
    * @param i &lq20
    * @return Factorial of i
     * @throws monalisa.addons.tokensimulator.Utilities.FactorialTooBigException
    */
    public static long factorial(Long i) throws FactorialTooBigException{
        if (i <= 20){
            return FACTORIAL[i.intValue()];
        }                
        throw new FactorialTooBigException();        
    }

    /**
     * Calculate natural logarithm of factorial of i. Used for calculating binomial coefficient.
     * @param i
     * @return 
     */
    public static double factorialln(long i){
        if (i <= 20){
            return Math.log(FACTORIAL[Long.valueOf(i).intValue()]);
        }
        return gammaln(i+1.0F);
    }
    
    /**
    * Calculate the binomial coefficient n choose k.
    * n choose k = n!  / (k!*(n-k)!).
    * @param n
    * @param k
    * @return 
    */
    public static long binomialCoefficient(long n, long k){
        if (k > n){
            return 0;
        }
        if (k == n || k == 0){
            return 1;
        }
        if (k == 1|| (k+1) == n){
            return n;
        }        
        
        double factN, factK, factNK;
        if (n <= 20){
            factN = FACTORIAL[(int) n];
        }
        else{
            factN = Math.exp(factorialln(n));
        }
        if (k <= 20){
            factK = FACTORIAL[(int) k];
        }
        else{
            factK = Math.exp(factorialln(k));
        }
        if (n - k <= 20){
            factNK = FACTORIAL[(int) (n-k)];
        }
        else{
            factNK = Math.exp(factorialln(n-k));
        }
        return Math.round(factN / (factK * factNK));
    }
    
    /**
     * Convert a number of seconds to the readable string representation if format "days hours minutes seconds".
     * @param seconds
     * @return 
     */
    public static String secondsToTime(double seconds){
        long days = TimeUnit.SECONDS.toDays((Math.round(seconds)));
        long hours = TimeUnit.SECONDS.toHours(Math.round(seconds)) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.SECONDS.toMinutes(Math.round(seconds)) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.DAYS.toMinutes(days);
        long sec = Math.round(seconds) - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.DAYS.toSeconds(days);
        return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, sec);
    }
}
