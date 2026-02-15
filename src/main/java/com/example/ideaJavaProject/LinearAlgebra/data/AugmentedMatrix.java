package com.example.ideaJavaProject.LinearAlgebra.data;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 拡大係数行列 [A|b] を表す。
 * A が (m×n)、b が (m×1) のとき、[A|b] は (m×(n+1))。
 */
public final class AugmentedMatrix {

    private final Matrix ab;

    private AugmentedMatrix(Matrix ab) {
        this.ab = ab;
    }

    public static AugmentedMatrix of(CoefficientMatrix a, ConstantVector b) {
        Objects.requireNonNull(a, "a must not be null");
        Objects.requireNonNull(b, "b must not be null");

        if (a.rows() != b.size()) {
            throw new IllegalArgumentException("row mismatch: a.rows=" + a.rows() + ", b.rows=" + b.size());
        }

        int m = a.rows();
        int n = a.cols();

        BigDecimal[][] values = new BigDecimal[m][n + 1];
        Matrix am = a.value();
        Matrix bm = b.value();

        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                values[r][c] = am.get(r, c);
            }
            values[r][n] = bm.get(r, 0);
        }
        return new AugmentedMatrix(Matrix.of(values));
    }

    public Matrix value() {
        return ab;
    }

    public int rows() {
        return ab.rows();
    }

    public int cols() {
        return ab.cols();
    }
}