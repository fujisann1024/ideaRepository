package com.example.ideaJavaProject.Common;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Objects;

/**
 * 有理数を表すクラス。
 *
 * <p>
 * 内部的には分子・分母を {@link BigInteger} で保持し、常に既約化された形（最大公約数で割り、分母は正）で表現します。
 * これにより、値の等価性は分子・分母の等価性と一致し、比較やハッシュコード計算が簡単になります。
 * </p>
 */
public final class Rational implements Comparable<Rational> {

    /** 0 を表す定数（0/1）。 */
    public static final Rational ZERO = new Rational(BigInteger.ZERO, BigInteger.ONE);

    /** 1 を表す定数（1/1）。 */
    public static final Rational ONE  = new Rational(BigInteger.ONE, BigInteger.ONE);

    /** 分子（負・0・正のいずれも可）。 */
    private final BigInteger num;

    /** 分母（常に正）。 */
    private final BigInteger den;

    /**
     * コンストラクタ（外部からの直接生成は禁止）。
     *
     * <p>
     * 本クラスのインスタンスは必ず {@link #of(BigInteger, BigInteger)} を経由して生成し、
     * 「分母正化」「既約化」「0 の正規化」を強制します。
     * </p>
     */
    private Rational(BigInteger num, BigInteger den) {
        this.num = num;
        this.den = den;
    }

    /**
     * 分子・分母（long）から {@link Rational} を生成します。
     *
     * <p>
     * {@link #of(BigInteger, BigInteger)} に委譲し、正規化（既約化・分母正化）を行います。
     * </p>
     *
     * @param numerator 分子
     * @param denominator 分母（0 不可）
     * @return 正規化された有理数
     * @throws ArithmeticException 分母が 0 の場合
     */
    public static Rational of(long numerator, long denominator) {
        return of(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    /**
     * 整数（long）を {@code integer/1} として {@link Rational} を生成します。
     *
     * @param integer 整数値
     * @return {@code integer/1} を表す有理数
     */
    public static Rational of(long integer) {
        return new Rational(BigInteger.valueOf(integer), BigInteger.ONE);
    }

    /**
     * 分子・分母から 既約分数を生成します。
     *
     * @param numerator 分子（null 不可）
     * @param denominator 分母（null 不可、0 不可）
     * @return 正規化された有理数
     * @throws NullPointerException 引数が null の場合
     * @throws ArithmeticException 分母が 0 の場合
     */
    public static Rational of(BigInteger numerator, BigInteger denominator) {
        Objects.requireNonNull(numerator, "numerator");
        Objects.requireNonNull(denominator, "denominator");
        
        // 0 除算の禁止
        if (denominator.signum() == 0) {
            throw new ArithmeticException("denominator is zero");
        }
        
        // 分子が 0 の場合は分母に関わらず 0（0/1）とする
        if (numerator.signum() == 0) {
            return ZERO;
        }

        // 分母が負なら分子・分母に -1 を掛けて分母を正にする
        BigInteger n = numerator;
        BigInteger d = denominator;
        if (d.signum() < 0) {
            n = n.negate();
            d = d.negate();
        }

        // 最大公約数で割って既約分数（これ以上約分できない形）にする
        BigInteger g = n.gcd(d);
        n = n.divide(g);
        d = d.divide(g);

        // canonical zero（念押し）
        if (n.signum() == 0) return ZERO;

        return new Rational(n, d);
    }

    /**
     * 文字列を {@link Rational} に変換します。
     *
     * <p>受理する形式：</p>
     * <ul>
     *   <li>{@code "3/5"} のような分数</li>
     *   <li>{@code "-7/2"} のような負の分数</li>
     *   <li>{@code "10"} のような整数（分母は 1）</li>
     * </ul>
     *
     * <p>
     * 解析後は {@link #of(BigInteger, BigInteger)} を通すため、既約化・分母正化が適用されます。
     * </p>
     *
     * @param s 入力文字列（null 不可）
     * @return 正規化された有理数
     * @throws NullPointerException s が null の場合
     * @throws NumberFormatException 数として解釈できない場合
     * @throws ArithmeticException 分母が 0 の場合（例：{@code "1/0"}）
     */
    public static Rational parse(String s) {
        Objects.requireNonNull(s, "s");
        String t = s.trim();
        int slash = t.indexOf('/');
        if (slash < 0) {
            return of(new BigInteger(t), BigInteger.ONE);
        }
        BigInteger n = new BigInteger(t.substring(0, slash).trim());
        BigInteger d = new BigInteger(t.substring(slash + 1).trim());
        return of(n, d);
    }

    /**
     * 分子を返します（既約化後の値）。
     *
     * @return 分子
     */
    public BigInteger numerator()   { return num; }

    /**
     * 分母を返します（常に正、既約化後の値）。
     *
     * @return 分母（正）
     */
    public BigInteger denominator() { return den; }

    /**
     * 符号を返します。
     *
     * @return -1（負）/ 0（ゼロ）/ 1（正）
     */
    public int signum() { return num.signum(); }

    /**
     * 0 かどうか判定します。
     *
     * @return 0 の場合 true
     */
    public boolean isZero() { return num.signum() == 0; }

    /**
     * 1 かどうか判定します。
     *
     * <p>既約化されているため、{@code num == den} は {@code 1/1} のみを意味します。</p>
     *
     * @return 1 の場合 true
     */
    public boolean isOne()  { return num.equals(den); }

    /**
     * 符号反転（-x）を返します。
     *
     * <p>0 の場合は {@link #ZERO} をそのまま返します。</p>
     *
     * @return -this
     */
    public Rational negate() {
        if (this == ZERO) return ZERO;
        return new Rational(num.negate(), den);
    }

    /**
     * 絶対値 |x| を返します。
     *
     * <p>すでに非負の場合は自身を返し、不要なインスタンス生成を避けます。</p>
     *
     * @return |this|
     */
    public Rational abs() {
        return signum() < 0 ? negate() : this;
    }

    /**
     * 逆数（1/x）を返します。
     *
     * @return this の逆数
     * @throws ArithmeticException this が 0 の場合（0 の逆数は未定義）
     */
    public Rational reciprocal() {
        if (isZero()) throw new ArithmeticException("division by zero (reciprocal of 0)");
        return of(den, num);
    }

    /**
     * 加算（this + other）を返します。
     *
     * <p>計算式：{@code a/b + c/d = (ad + bc) / bd}</p>
     *
     * @param other 加算相手（null 不可）
     * @return this + other
     */
    public Rational add(Rational other) {
        Objects.requireNonNull(other, "other");
        if (this.isZero()) return other;
        if (other.isZero()) return this;

        BigInteger n = this.num.multiply(other.den).add(other.num.multiply(this.den));
        BigInteger d = this.den.multiply(other.den);
        return of(n, d);
    }

    /**
     * 減算（this - other）を返します。
     *
     * @param other 減算相手（null 不可）
     * @return this - other
     */
    public Rational subtract(Rational other) {
        return add(other.negate());
    }

    /**
     * 乗算（this * other）を返します。
     *
     * @param other 乗算相手（null 不可）
     * @return this * other
     */
    public Rational multiply(Rational other) {
        Objects.requireNonNull(other, "other");
        if (this.isZero() || other.isZero()) return ZERO;

        BigInteger a = this.num;
        BigInteger b = this.den;
        BigInteger c = other.num;
        BigInteger d = other.den;

        // クロス約分を先に行って中間値の膨張を抑える
        BigInteger g1 = a.gcd(d);
        a = a.divide(g1);
        d = d.divide(g1);

        BigInteger g2 = c.gcd(b);
        c = c.divide(g2);
        b = b.divide(g2);

        return of(a.multiply(c), b.multiply(d));
    }

    /**
     * 除算（this / other）を返します。
     *
     * <p>定義：{@code a/b ÷ c/d = a/b × d/c}</p>
     *
     * @param other 除算相手（null 不可）
     * @return this / other
     * @throws ArithmeticException other が 0 の場合（0 で割ることはできない）
     */
    public Rational divide(Rational other) {
        Objects.requireNonNull(other, "other");
        return multiply(other.reciprocal());
    }

    /**
     * 整数指数の冪（this^exponent）を返します。
     *
     * <ul>
     *   <li>{@code exponent == 0} のとき 1</li>
     *   <li>{@code exponent < 0} のとき逆数の正の冪（{@code (1/this)^{-exponent}}）</li>
     *   <li>{@code 0^負} は未定義のため例外</li>
     * </ul>
     *
     * @param exponent 指数（int）
     * @return this^exponent
     * @throws ArithmeticException this が 0 かつ exponent < 0 の場合
     */
    public Rational pow(int exponent) {
        if (exponent == 0) return ONE;
        if (this.isZero()) {
            if (exponent < 0) throw new ArithmeticException("0 cannot be raised to negative power");
            return ZERO;
        }
        if (exponent < 0) return this.reciprocal().pow(-exponent);

        BigInteger n = num.pow(exponent);
        BigInteger d = den.pow(exponent);
        return of(n, d);
    }

    /**
     * {@link BigDecimal} に変換します（近似値）。
     *
     * @param mc 精度・丸め設定（null 不可）
     * @return 近似小数値
     */
    public BigDecimal toBigDecimal(MathContext mc) {
        Objects.requireNonNull(mc, "mc");
        return new BigDecimal(num).divide(new BigDecimal(den), mc);
    }

    /**
     * 大小比較を行います。
     *
     * @param o 比較対象（null 不可）
     * @return this &lt; o なら負、等しければ 0、this &gt; o なら正
     */
    @Override
    public int compareTo(Rational o) {
        Objects.requireNonNull(o, "o");
        if (this == o) return 0;
        // クロス積で比較(分母は正なので符号を気にせずそのまま比較できる)
        return this.num.multiply(o.den).compareTo(o.num.multiply(this.den));
    }

    /**
     * 値の等価性を判定します。
     *
     * @param obj 比較対象
     * @return 等しい場合 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Rational r)) return false;
        // 分子・分母が同じなら同じ値（正規化されているため）
        return num.equals(r.num) && den.equals(r.den);
    }

    /**
     * ハッシュ値を返します。
     *
     * <p>{@link #equals(Object)} と整合するよう、分子・分母から計算します。</p>
     *
     * @return ハッシュ値
     */
    @Override
    public int hashCode() {
        return Objects.hash(num, den);
    }

    /**
     * 文字列表現を返します。
     *
     * @return 文字列表現
     */
    @Override
    public String toString() {
        if (den.equals(BigInteger.ONE)) return num.toString();
        return num + "/" + den;
    }
}