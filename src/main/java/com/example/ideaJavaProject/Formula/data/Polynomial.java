package com.example.ideaJavaProject.Formula.data;

import java.math.BigInteger;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 多項式計算
 */
public final class Polynomial {
    
	/**
	 * 次数 -> 係数の対応表
	 */
    private final NavigableMap<Integer, BigInteger> terms;

    private Polynomial(NavigableMap<Integer, BigInteger> normalized) {
        this.terms = normalized;
    }

    /** 0 多項式 */
    public static Polynomial zero() {
        return new Polynomial(new TreeMap<>());
    }

    /**
     * 
     * @param c
     * @return
     */
    public static Polynomial constant(BigInteger c) {
        if (c.equals(BigInteger.ZERO)) return zero();
        TreeMap<Integer, BigInteger> m = new TreeMap<>();
        m.put(0, c);
        return new Polynomial(m);
    }

    public static Polynomial monomial(BigInteger a, int deg) {
        if (deg < 0) throw new IllegalArgumentException("degree must be >= 0");
        if (a.equals(BigInteger.ZERO)) return zero();
        TreeMap<Integer, BigInteger> m = new TreeMap<>();
        m.put(deg, a);
        return new Polynomial(m);
    }

    /** 係数配列から多項式生成: coeffs[k] が x^k の係数 */
    public static Polynomial fromCoeffs(long... coeffs) {
        TreeMap<Integer, BigInteger> m = new TreeMap<>();
        for (int k = 0; k < coeffs.length; k++) {
            BigInteger a = BigInteger.valueOf(coeffs[k]);
            if (!a.equals(BigInteger.ZERO)) m.put(k, a);
        }
        return new Polynomial(m);
    }

    /** 次数（0多項式は -1） */
    public int degree() {
        return terms.isEmpty() ? -1 : terms.lastKey();
    }

    /**
     * 次数が存在するか
     * @return
     */
    public boolean isZero() {
        return terms.isEmpty();
    }

    /**
     * 係数取得
     * @param deg
     * @return
     */
    public BigInteger coeff(int deg) {
        return terms.getOrDefault(deg, BigInteger.ZERO);
    }

    /** 正規化（0係数除去） */
    private static TreeMap<Integer, BigInteger> normalize(Map<Integer, BigInteger> raw) {
        TreeMap<Integer, BigInteger> m = new TreeMap<>();
        for (var e : raw.entrySet()) {
            int d = e.getKey();
            BigInteger a = e.getValue();
            if (d < 0) throw new IllegalArgumentException("degree must be >= 0");
            if (a == null) throw new NullPointerException("coefficient is null");
            if (!a.equals(BigInteger.ZERO)) m.put(d, a);
        }
        return m;
    }

    /** 加算 */
    public Polynomial add(Polynomial other) {
        Objects.requireNonNull(other);
        TreeMap<Integer, BigInteger> m = new TreeMap<>(this.terms);
        for (var e : other.terms.entrySet()) {
            m.merge(e.getKey(), e.getValue(), BigInteger::add);
        }
        return new Polynomial(normalize(m));
    }

    /** 減算 */
    public Polynomial subtract(Polynomial other) {
        Objects.requireNonNull(other);
        return add(other.negate());
    }

    /** 符号反転 */
    public Polynomial negate() {
        TreeMap<Integer, BigInteger> m = new TreeMap<>();
        for (var e : this.terms.entrySet()) {
            m.put(e.getKey(), e.getValue().negate());
        }
        return new Polynomial(m);
    }

    /** スカラー倍 */
    public Polynomial scale(long c) {
        return scale(BigInteger.valueOf(c));
    }

    public Polynomial scale(BigInteger c) {
        if (c.equals(BigInteger.ZERO)) return zero();
        if (c.equals(BigInteger.ONE)) return this;
        TreeMap<Integer, BigInteger> m = new TreeMap<>();
        for (var e : this.terms.entrySet()) {
            m.put(e.getKey(), e.getValue().multiply(c));
        }
        return new Polynomial(normalize(m));
    }

    /** 乗算（畳み込み） */
    public Polynomial multiply(Polynomial other) {
        Objects.requireNonNull(other);
        if (this.isZero() || other.isZero()) return zero();
        TreeMap<Integer, BigInteger> m = new TreeMap<>();
        for (var a : this.terms.entrySet()) {
            for (var b : other.terms.entrySet()) {
                int deg = a.getKey() + b.getKey();
                BigInteger coef = a.getValue().multiply(b.getValue());
                m.merge(deg, coef, BigInteger::add);
            }
        }
        return new Polynomial(normalize(m));
    }

    /** 値の評価 P(x)（Horner法） */
    public BigInteger evaluate(BigInteger x) {
        Objects.requireNonNull(x);
        if (this.isZero()) return BigInteger.ZERO;
        // Horner: 高次から降ろす（疎でもOKにする）
        int max = degree();
        BigInteger acc = BigInteger.ZERO;
        for (int d = max; d >= 0; d--) {
            acc = acc.multiply(x).add(coeff(d));
        }
        return acc;
    }

    /**
     * 多項式の割り算（整除）: this = q*div + r
     * 係数が整数なので「leading係数で割り切れる」ケースのみ想定。
     */
    public DivMod divModExact(Polynomial div) {
        Objects.requireNonNull(div);
        if (div.isZero()) throw new ArithmeticException("divide by zero polynomial");
        if (this.isZero()) return new DivMod(zero(), zero());

        TreeMap<Integer, BigInteger> r = new TreeMap<>(this.terms);
        TreeMap<Integer, BigInteger> q = new TreeMap<>();

        int dDiv = div.degree();
        BigInteger lcDiv = div.coeff(dDiv);

        while (!r.isEmpty() && r.lastKey() >= dDiv) {
            int dR = r.lastKey();
            BigInteger lcR = r.get(dR);

            int dQ = dR - dDiv;

            BigInteger[] dr = lcR.divideAndRemainder(lcDiv);
            if (!dr[1].equals(BigInteger.ZERO)) {
                throw new ArithmeticException("not divisible exactly: leading coefficient remainder != 0");
            }
            BigInteger cQ = dr[0];

            // q += cQ * x^dQ
            q.merge(dQ, cQ, BigInteger::add);

            // r -= (cQ * x^dQ) * div
            for (var e : div.terms.entrySet()) {
                int d = e.getKey() + dQ;
                BigInteger sub = e.getValue().multiply(cQ);
                r.merge(d, sub.negate(), BigInteger::add);
                if (r.get(d).equals(BigInteger.ZERO)) r.remove(d);
            }
        }

        return new DivMod(new Polynomial(normalize(q)), new Polynomial(normalize(r)));
    }

    /** 割り算結果 */
    public record DivMod(
    		/** 商*/
    		Polynomial quotient
    		/** 余り*/
    		, Polynomial remainder) {}

    @Override
    public String toString() {
        if (terms.isEmpty()) return "0";
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        // 高次→低次で表示
        for (var e : terms.descendingMap().entrySet()) {
            int d = e.getKey();
            BigInteger a = e.getValue();

            boolean neg = a.signum() < 0;
            BigInteger abs = a.abs();

            if (first) {
                if (neg) sb.append("-");
            } else {
                sb.append(neg ? " - " : " + ");
            }

            if (d == 0) {
                sb.append(abs);
            } else {
                // 係数1は省略（ただし - は別処理済み）
                if (!abs.equals(BigInteger.ONE)) sb.append(abs).append("*");
                sb.append("x");
                if (d != 1) sb.append("^").append(d);
            }

            first = false;
        }
        return sb.toString();
    }

}
