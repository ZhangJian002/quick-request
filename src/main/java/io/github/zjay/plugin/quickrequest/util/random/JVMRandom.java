package io.github.zjay.plugin.quickrequest.util.random;

import java.util.Random;

public final class JVMRandom extends Random {
    private static final long serialVersionUID = 1L;
    private boolean constructed = false;

    public JVMRandom() {
        this.constructed = true;
    }

    public synchronized void setSeed(long seed) {
        if (this.constructed) {
            throw new UnsupportedOperationException();
        }
    }

    public synchronized double nextGaussian() {
        throw new UnsupportedOperationException();
    }

    public void nextBytes(byte[] byteArray) {
        throw new UnsupportedOperationException();
    }

    public int nextInt() {
        return this.nextInt(Integer.MAX_VALUE);
    }

    public int nextInt(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Upper bound for nextInt must be positive");
        } else {
            return (int)(Math.random() * (double)n);
        }
    }

    public long nextLong() {
        return nextLong(Long.MAX_VALUE);
    }

    public static long nextLong(long n) {
        if (n <= 0L) {
            throw new IllegalArgumentException("Upper bound for nextInt must be positive");
        } else {
            return (long)(Math.random() * (double)n);
        }
    }

    public boolean nextBoolean() {
        return Math.random() > 0.5;
    }

    public float nextFloat() {
        return (float)Math.random();
    }

    public double nextDouble() {
        return Math.random();
    }
}
