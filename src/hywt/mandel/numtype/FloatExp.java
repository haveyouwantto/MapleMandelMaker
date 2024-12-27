package hywt.mandel.numtype;

import java.math.BigDecimal;

public class FloatExp {
    private static final double LOG2_10 = Math.log(10) / Math.log(2);
    private static final double LOG10_2 = Math.log(2) / Math.log(10);
    private static double[] expTable;

    static {
        expTable = new double[324 + 308];
        for (int i = 0; i < 324 + 308; i++) {
            expTable[i] = Math.pow(10, i - 324);
        }
    }

    private double base;
    private int exp;

    public FloatExp(double base, int exp) {
        if (Double.isNaN(base) || !Double.isFinite(base) || !Double.isFinite(exp)) {
            throw new IllegalArgumentException("Invalid FloatExp");
        }
        this.base = base;
        this.exp = exp;
        norm();
    }

    public FloatExp(double val) {
        this(val, 0);
    }

    private FloatExp norm() {
        if (this.base == 0) {
            this.exp = 0;
        }
        else if (this.base > 10 || this.base < 1) {
            int exp = getExpOfDouble(this.base);
            this.exp += exp;
            this.base /= getExp(exp);
        }
        return this;
    }

    private static double getExp(int exp) {
        if (exp < -324) return 0;
        else if (exp > 308) return Double.POSITIVE_INFINITY;
        return expTable[exp + 324];
    }

    private static int getExpOfDouble(double d) {
        return (int) Math.floor(Math.log(Math.abs(d)) * 0.4342944819032518);
    }

    public double doubleValue() {
        return this.base * getExp(this.exp);
    }

    public int scale() {
        norm();
        return exp;
    }

    @Override
    public String toString() {
        norm();
        return this.base + "e" + this.exp;
    }

    public FloatExp add(FloatExp other) {
        if (other.base == 0) return this;
        else if (base == 0) return other;
        int expDiff = other.exp - exp;
        if (expDiff == 0) {
            return new FloatExp(base + other.base, exp);
        } else if (expDiff > 16) {
            return other;
        } else {
            return new FloatExp(base + other.base * getExp(expDiff), exp);
        }
    }

    public FloatExp addMut(FloatExp other) {
        if (other.base == 0) return this;
        else if (base == 0) {
            base = other.base;
            exp = other.exp;
            return this;
        }
        int expDiff = other.exp - exp;
        if (expDiff == 0) {
            base += other.base;
            return this.norm();
        } else if (expDiff > 16) {
            base = other.base;
            exp = other.exp;
            return this;
        } else {
            base += other.base * getExp(expDiff);
            return this.norm();
        }
    }

    public FloatExp sub(FloatExp other) {
        if (other.base == 0) return this;
        else if (base == 0) return other.rev();
        int expDiff = other.exp - this.exp;
        if (expDiff == 0) {
            return new FloatExp(this.base - other.base, this.exp);
        } else if (expDiff > 16) {
            return other.rev();
        } else {
            return new FloatExp(this.base - other.base * getExp(expDiff), this.exp);
        }
    }

    public FloatExp subMut(FloatExp other) {
        if (other.base == 0) return this;
        else if (base == 0) {
            base = -other.base;
            exp = other.exp;
            return this;
        }
        int expDiff = other.exp - this.exp;
        if (expDiff == 0) {
            this.base -= other.base;
            return this.norm();
        } else if (expDiff > 16) {
            base = -other.base;
            exp = other.exp;
            return this;
        } else {
            this.base -= other.base * getExp(expDiff);
            return this.norm();
        }
    }

    public FloatExp mul(FloatExp other) {
        return new FloatExp(this.base * other.base, this.exp + other.exp);
    }

    public FloatExp mulMut(FloatExp other) {
        this.base *= other.base;
        this.exp += other.exp;
        return this.norm();
    }

    public FloatExp div(FloatExp other) {
        if (other.base == 0) throw new ArithmeticException("divide by 0");
        return new FloatExp(this.base / other.base, this.exp - other.exp);
    }

    public FloatExp divMut(FloatExp other) {
        if (other.base == 0) throw new ArithmeticException("divide by 0");
        this.base /= other.base;
        this.exp -= other.exp;
        return this.norm();
    }

    public FloatExp add(double other) {
        return add(new FloatExp(other));
    }

    public FloatExp sub(double other) {
        return sub(new FloatExp(other));
    }

    public FloatExp mul(double other) {
        return mul(new FloatExp(other));
    }

    public FloatExp div(double other) {
        return div(new FloatExp(other));
    }

    public FloatExp abs() {
        return new FloatExp(Math.abs(this.base), this.exp);
    }

    public FloatExp sqrt() {
        if (this.base < 0) throw new IllegalArgumentException("Cannot take square root of negative number");
        return new FloatExp(Math.sqrt(this.base), this.exp / 2);
    }

    public FloatExp rev() {
        return new FloatExp(-this.base, this.exp);
    }

    public FloatExp copy() {
        return new FloatExp(this.base, this.exp);
    }

    public FloatExp square() {
        return new FloatExp(this.base * this.base, this.exp * 2);
    }

    public FloatExp squareMut() {
        this.base *= this.base;
        this.exp *= 2;
        return norm();
    }

    public int compareTo(FloatExp other) {
        if (this.base > 0 && other.base < 0) return 1;
        if (this.base < 0 && other.base > 0) return -1;
        if (this.base == 0 && other.base == 0) return 0;
        if (this.base == 0) return other.base > 0 ? -1 : 1;
        if (other.base == 0) return this.base > 0 ? 1 : -1;

        if (this.exp > other.exp) return this.base > 0 ? 1 : -1;
        if (this.exp < other.exp) return this.base > 0 ? -1 : 1;

        return Double.compare(this.base, other.base);
    }

    public double log2Value() {
        double log2Mantissa = Math.log(this.base) / Math.log(2);
        double log2Exponent = this.exp * LOG2_10;
        return log2Mantissa + log2Exponent;
    }

    public static FloatExp fromLog2(double val) {
        double log10 = val * LOG10_2;
        return new FloatExp(Math.pow(10, log10 - Math.floor(log10)), (int) Math.floor(log10));
    }

    public static FloatExp parseFloatExp(String string) {
        // Parse the string into base and exponent parts
        String[] parts = string.toLowerCase().split("e");
        double base = Double.parseDouble(parts[0]);
        int exponent = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

        // Return a new FloatExp instance
        return new FloatExp(base, exponent);
    }

    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(base).scaleByPowerOfTen(exp);
    }

    public static FloatExp fromDouble(double num) {
        if (num == 0) {
            return new FloatExp(0, 0);
        }
        // Get the exponent
        int exponent = (int) Math.floor(Math.log10(Math.abs(num)));
        // Calculate the significand
        double significand = num / getExp(exponent);
        return new FloatExp(significand, exponent);
    }

    public static FloatExp decimalToFloatExp(BigDecimal num) {
        if (num.compareTo(BigDecimal.ZERO) == 0) return new FloatExp(0, 0);

        // Normalize the number
        int scale = num.scale();
        int precision = num.precision();
        int exponent = precision - scale - 1;

        // Normalize the value to [1, 10)
        BigDecimal normalized = num.movePointLeft(exponent);
        return new FloatExp(normalized.doubleValue(), exponent);
    }

    public static FloatExp max(FloatExp a, FloatExp b) {
        if (a.compareTo(b) > 0) return a;
        else return b;
    }

    public static FloatExp min(FloatExp a, FloatExp b) {
        if (a.compareTo(b) < 0) return a;
        else return b;
    }
}
