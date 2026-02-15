package com.example.ideaJavaProject.LinearAlgebra.data;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 連立一次方程式 A x = b における定数項ベクトル b（列ベクトル）を表す。
 * 形状は (m×1) を要求する。
 */
public final class ConstantVector {

    private final Matrix b; // m×1

    public ConstantVector(Matrix b) {
        this.b = Objects.requireNonNull(b, "b must not be null");
        if (b.cols() != 1) {
            throw new IllegalArgumentException("b must be a column vector (m×1): cols=" + b.cols());
        }
    }

    public Matrix value() {
        return b;
    }

    public int size() {
        return b.rows();
    }

    public BigDecimal get(int row) {
        return b.get(row, 0);
    }
}