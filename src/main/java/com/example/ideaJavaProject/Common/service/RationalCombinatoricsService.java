package com.example.ideaJavaProject.Common.service;

import java.math.BigInteger;

import com.example.ideaJavaProject.Common.data.Rational;

/**
 * 階乗・順列・組合せを、厳密計算（BigInteger）した上で Rational で返すサービス
 *
 * <p>注意：階乗や nCk 自体は整数だが、確率では比として分数が必要になるため、
 * 返り値を Rational に統一しておくと後段（確率計算）と接続しやすい。</p>
 * 
 * 
 * <p>n個すべてを並べる並び方（全員の順番）は n! 通り。</p>
 * <p>n個の中からk個を選んで並べる並び方は P(n,k) = n!/(n-k)! 通り。</p>
 * <p>n個の中からk個を選ぶ方法は C(n,k) = n!/(k!(n-k)</p>
 * 
 */
public class RationalCombinatoricsService {
	/** n! を返す（n>=0） */
    public static Rational factorial(int n) {
        return Rational.of(factorialBig(n), BigInteger.ONE);
    }

    /** P(n,k) = n!/(n-k)!（0<=k<=n） */
    public static Rational perm(int n, int k) {
        return Rational.of(permBig(n, k), BigInteger.ONE);
    }

    /** C(n,k) = n!/(k!(n-k)!)（0<=k<=n） */
    public static Rational comb(int n, int k) {
        return Rational.of(combBig(n, k), BigInteger.ONE);
    }

    /**
     * 確率で頻出：C(n,k)/C(N,K) を Rational で返す。
     * 例：超幾何分布など。
     */
    public static Rational combRatio(int n, int k, int N, int K) {
        BigInteger a = combBig(n, k);
        BigInteger b = combBig(N, K);
        return Rational.of(a, b); // of(...) が約分してくれる
    }

    // -----------------------
    // BigInteger 計算（本体）
    // -----------------------

    private static BigInteger factorialBig(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0: n=" + n);
        BigInteger r = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            r = r.multiply(BigInteger.valueOf(i));
        }
        return r;
    }

    private static BigInteger permBig(int n, int k) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0: n=" + n);
        if (k < 0 || k > n) throw new IllegalArgumentException("require 0<=k<=n: n=" + n + ", k=" + k);

        // P(n,k) = n(n-1)...(n-k+1)
        BigInteger r = BigInteger.ONE;
        for (int i = 0; i < k; i++) {
            r = r.multiply(BigInteger.valueOf(n - i));
        }
        return r;
    }

    /**
     * C(n,k) を整数として計算（BigInteger）。
     * 乗算と除算を逐次で行う「乗除の形」なので、巨大な n! を作らずに済む。
     *
     * C(n,k) = Π_{i=1..k} (n-k+i)/i  （k = min(k, n-k)）
     */
    private static BigInteger combBig(int n, int k) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0: n=" + n);
        if (k < 0 || k > n) throw new IllegalArgumentException("require 0<=k<=n: n=" + n + ", k=" + k);
        if (k == 0 || k == n) return BigInteger.ONE;

        int kk = Math.min(k, n - k);
        BigInteger r = BigInteger.ONE;

        for (int i = 1; i <= kk; i++) {
            // r = r * (n - kk + i) / i を常に整数で保つ
            BigInteger mul = BigInteger.valueOf(n - kk + i);
            BigInteger div = BigInteger.valueOf(i);

            // r*mul を先にしてから割ると膨らむことがあるので、適宜 gcd で簡約
            BigInteger g1 = mul.gcd(div);
            mul = mul.divide(g1);
            div = div.divide(g1);

            BigInteger g2 = r.gcd(div);
            r = r.divide(g2);
            div = div.divide(g2);

            r = r.multiply(mul);
            // ここで div は 1 になる（整数性が保証される）
            if (!div.equals(BigInteger.ONE)) {
                r = r.divide(div);
            }
        }
        return r;
    }
}
