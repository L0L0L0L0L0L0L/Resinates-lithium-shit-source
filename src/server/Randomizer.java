package server;

import java.util.Random;

public class Randomizer {

    private final static Random rand = new Random();

    public static final int nextInt() {

        return rand.nextInt();
    }

    public static final int nextInt(final int arg0) {

        return rand.nextInt(arg0);
    }

    public static final double nextDouble(final double arg0) {

        return rand.nextDouble(arg0);
    }

    public static final long nextlong(final long arg0) {

        return rand.nextLong(arg0);
    }

    public static final void nextBytes(final byte[] bytes) {

        rand.nextBytes(bytes);
    }

    public static final boolean nextBoolean() {

        return rand.nextBoolean();
    }

    public static final double nextDouble() {

        return rand.nextDouble();
    }

    public static final float nextFloat() {

        return rand.nextFloat();
    }

    public static final long nextLong() {

        return rand.nextLong();
    }

    public static final int rand(final int lbound, final int ubound) {

        return nextInt(ubound - lbound + 1) + lbound;
    }

    public static int random(int min, int max) {
        if (max != 0) {
            if (min > max) {
                return max;
            }
            return nextInt(max - min + 1) + min;
        } else {
            return 0;
        }

    }

    public static int random(int max) {
        return nextInt(max) + 1;
    }

    public static double randomDouble(double min, double max) {
        Random r = new Random();
        return r.nextDouble() * (max - min) + min;
    }

    public static int randomMinMax(int min, int max) {
        return nextInt(max - min + 1) + min;
    }

    public static int randomMinMaxCap(long min, long max, int cap) {
        long tmax = Lmin(max, min) * 4;
        long value = nextlong(tmax - min) + min;
        return (int) MaxLong(value, cap);
    }

    public static long Lmin(long value, long min) {
        return value >= min ? value : min;
    }

    public static int MinMax(int value, int min, int max) {
        if (value < min) {
            value = min;
        }
        if (value > max) {
            value = max;
        }
        return value;
    }

    public static double MinMaxDouble(double value, double min, double max) {
        if (value < min) {
            value = min;
        }
        if (value > max) {
            value = max;
        }
        return value;
    }

    public static long MinMaxLong(long value, long min, long max) {
        if (value < min) {
            value = min;
        }
        if (value > max) {
            value = max;
        }
        return value;
    }

    public static int Min(int value, int min) {
        return value < min ? min : value;
    }

    public static double MinDouble(double value, double min) {
        return value < min ? min : value;
    }

    public static long LongMin(long value, long min) {
        return value < min ? min : value;
    }

    public static long LongMax(long value, long max) {
        return value > max ? max : value;
    }

    public static short MaxShort(short value, short max) {
        return value > max ? max : value;
    }

    public static int Max(int value, int max) {
        return value > max ? max : value;
    }

    public static long MaxLong(long value, long max) {
        return value > max ? max : value;
    }

    public static double DoubleMax(double value, double max) {
        return value > max ? max : value;
    }

    public static double DoubleMin(double value, double min) {
        return value < min ? min : value;
    }

    public static double DoubleMinMax(double value, double min, double max) {
        value = value > max ? max : value;
        return value < min ? min : value;
    }
}
