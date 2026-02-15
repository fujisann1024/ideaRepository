package com.example.ideaJavaProject.LinearAlgebra.data;

import java.util.Objects;

/**
 * 連立一次方程式 A x = b における係数行列 A を表す。
 */
public final class CoefficientMatrix {

    private final Matrix a;

    public CoefficientMatrix(Matrix a) {
        this.a = Objects.requireNonNull(a, "a must not be null");
    }

    public Matrix value() {
        return a;
    }

    public int rows() {
        return a.rows();
    }

    public int cols() {
        return a.cols();
    }

    public boolean isSquare() {
        return a.rows() == a.cols();
    }
}