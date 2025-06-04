package neo.util.other;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public final class MathUtil {

    public final SecureRandom RANDOM = new SecureRandom();

    public static final DecimalFormat DF_0 = new DecimalFormat("0");
    public static final DecimalFormat DF_1 = new DecimalFormat("0.0");
    public static final DecimalFormat DF_2 = new DecimalFormat("0.00");
    public static final DecimalFormat DF_1D = new DecimalFormat("0.#");
    public static final DecimalFormat DF_2D = new DecimalFormat("0.##");

    public double lerp(final double a, final double b, final double c) {
        return a + c * (b - a);
    }

    public float lerp(final float a, final float b, final float c) {
        return a + c * (b - a);
    }

    public boolean roughlyEquals(final double alpha, final double beta) {
        return Math.abs(alpha - beta) < 1.0E-4;
    }

    public static double roundToDecimal(double number, double places) {
        return Math.round(number * Math.pow(10, places)) / Math.pow(10, places);
    }

    public double round(final double value, final int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double getKurtosis(final Collection<? extends Number> data) {
        double sum = 0.0;
        int count = 0;
        for (final Number number : data) {
            sum += number.doubleValue();
            ++count;
        }
        if (count < 3.0) {
            return 0.0;
        }
        final double efficiencyFirst = count * (count + 1.0) / ((count - 1.0) * (count - 2.0) * (count - 3.0));
        final double efficiencySecond = 3.0 * Math.pow(count - 1.0, 2.0) / ((count - 2.0) * (count - 3.0));
        final double average = sum / count;
        double variance = 0.0;
        double varianceSquared = 0.0;
        for (final Number number2 : data) {
            variance += Math.pow(average - number2.doubleValue(), 2.0);
            varianceSquared += Math.pow(average - number2.doubleValue(), 4.0);
        }
        return efficiencyFirst * (varianceSquared / Math.pow(variance / sum, 2.0)) - efficiencySecond;
    }

    public static double getRandom(double min, double max) {
        if (min == max) {
            return min;
        } else if (min > max) {
            final double d = min;
            min = max;
            max = d;
        }
        return ThreadLocalRandom.current().nextDouble(min, max);
    }


    public double getVariance(final Collection<? extends Number> data) {
        int count = 0;

        double sum = 0.0;
        double variance = 0.0;

        final double average;

        for (final Number number : data) {
            sum += number.doubleValue();
            ++count;
        }

        average = sum / count;

        for (final Number number : data) {
            variance += Math.pow(number.doubleValue() - average, 2.0);
        }

        return variance;
    }

    public double getStandardDeviation(final Collection<? extends Number> data) {
        return Math.sqrt(getVariance(data));
    }


    public double getAverage(final Collection<? extends Number> data) {
        double sum = 0.0;

        for (final Number number : data) {
            sum += number.doubleValue();
        }

        return sum / data.size();
    }

    public static final SecureRandom secureRandom = new SecureRandom();

    public static int getRandomInRange(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static float roundToFloat(double d)
    {
        return (float)((double)Math.round(d * 1.0E8D) / 1.0E8D);
    }

    public static final float deg2Rad = roundToFloat(0.017453292519943295D);

    public static double[] yawPos(double value) {
        return yawPos(Minecraft.getMinecraft().thePlayer.rotationYaw * deg2Rad, value);
    }

    public static double[] yawPos(float yaw, double value) {
        return new double[]{-MathHelper.sin(yaw) * value, MathHelper.cos(yaw) * value};
    }

    public static float getRandomInRange(float min, float max) {
        SecureRandom random = new SecureRandom();
        return random.nextFloat() * (max - min) + min;
    }

    public static double getRandomInRange(double min, double max) {
        SecureRandom random = new SecureRandom();
        return min == max ? min : random.nextDouble() * (max - min) + min;
    }

    public static int getRandomNumberUsingNextInt(int min, int max) {
        java.util.Random random = new java.util.Random();
        return random.nextInt(max - min) + min;
    }

    public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static Vec3 interpolate(Vec3 end, Vec3 start, float multiple) {
        return new Vec3(
                interpolate(end.xCoord, start.xCoord, multiple),
                interpolate(end.yCoord, start.yCoord, multiple),
                interpolate(end.zCoord, start.zCoord, multiple));
    }



    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float) interpolationValue).floatValue();
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float) interpolationValue).intValue();
    }

    public static float calculateGaussianValue(float x, float sigma) {
        double output = 1.0 / Math.sqrt(2.0 * Math.PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }

    public static double roundToHalf(double d) {
        return Math.round(d * 2) / 2.0;
    }

    public static double round(double num, double increment) {
        BigDecimal bd = new BigDecimal(num);
        bd = (bd.setScale((int) increment, RoundingMode.HALF_UP));
        return bd.doubleValue();
    }

    public static String round(String value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.stripTrailingZeros();
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.toString();
    }

    public static float getRandomFloat(float max, float min) {
        SecureRandom random = new SecureRandom();
        return random.nextFloat() * (max - min) + min;
    }


    public static int getNumberOfDecimalPlace(double value) {
        final BigDecimal bigDecimal = new BigDecimal(value);
        return Math.max(0, bigDecimal.stripTrailingZeros().scale());
    }

    public double getCps(final Collection<? extends Number> data) {
        return 20.0D * getAverage(data);
    }
}