/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.util;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author
 * http://www.javamex.com/tutorials/random_numbers/numerical_recipes.shtml
 */
public class HighQualityRandom extends Random {

    private final Lock l = new ReentrantLock();
    private long u;
    private long v = 4101842887655102017L;
    private long w = 1;
    /**
     * Seed of the generator.
     */
    private long seed;

    public HighQualityRandom() {
        this(System.nanoTime());
    }

    public HighQualityRandom(long seedN) {
        this.seed = seedN;
        try {
            l.lock();
            u = seed ^ v;
            nextLong();
            v = u;
            nextLong();
            w = v;
            nextLong();
        } finally {
            l.unlock();
        }
    }

    @Override
    public long nextLong() {
        l.lock();
        try {
            u = u * 2862933555777941757L + 7046029254386353087L;
            v ^= v >>> 17;
            v ^= v << 31;
            v ^= v >>> 8;
            w = 4294957665L * (w & 0xffffffff) + (w >>> 32);
            long x = u ^ (u << 21);
            x ^= x >>> 35;
            x ^= x << 4;
            long ret = (x + v) ^ w;
            return ret;
        } finally {
            l.unlock();
        }
    }

    @Override
    protected int next(int bits) {
        return (int) (nextLong() >>> (64 - bits));
    }

    /**
     * Generates a pseudorandom number which is given by the Poisson
     * distribution with given mean. If the mean is greater than 100, the
     * poisson distribution is approximated by the normal (gaussian)
     * distribution with the given mean and the standard deviation equal to the
     * square root of the mean.
     *
     * @param mean
     * @return
     */
    public int nextPoisson(double mean) {
        if (mean >= 100) {
            return (int) (nextGaussian() * Math.sqrt(mean) + mean);
        }
        double lVal = Math.exp(-mean);
        int k = 0;
        double p = 1;
        do {
            k++;
            p = p * this.nextDouble();
        } while (p > lVal);
        return k - 1;
    }

    /**
     * Get the seed of generator.
     *
     * @return
     */
    public long getSeed() {
        return this.seed;
    }
}
