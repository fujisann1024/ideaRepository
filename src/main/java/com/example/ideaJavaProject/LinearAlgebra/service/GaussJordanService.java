package com.example.ideaJavaProject.LinearAlgebra.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

import com.example.ideaJavaProject.LinearAlgebra.data.AugmentedMatrix;
import com.example.ideaJavaProject.LinearAlgebra.data.Matrix;

/**
 * 掃き出し法（Gauss-Jordan elimination）。
 */
public final class GaussJordanService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    private GaussJordanService() {
        // utility
    }

    /**
     * 拡大係数行列を行基本変形して RREF（行最簡形）にする。
     *
     * @param augmented 拡大係数行列 [A|b]
     * @param mc BigDecimal の除算/乗算/加減算に適用する MathContext
     * @param eps 0判定用の許容誤差（|v| <= eps を 0 とみなす、0以上）
     * @return RREF 化された行列
     */
    public static Matrix rref(AugmentedMatrix augmented, MathContext mc, BigDecimal eps) {
        Objects.requireNonNull(augmented, "augmented must not be null");
        Objects.requireNonNull(mc, "mc must not be null");
        Objects.requireNonNull(eps, "eps must not be null");
        if (eps.signum() < 0) {
            throw new IllegalArgumentException("eps must be non-negative: eps=" + eps);
        }

        BigDecimal[][] matrix = augmented.value().toArray(); // 作業用（配列として deep copy）
        int row = matrix.length;
        int column = matrix[0].length;

        int pivotRow = 0;

        // pivotRow(いまピボット（基準）にする行) を 0..m-1 で(上から)動かしながら
        // pivotCol(いま注目している列) を 0..n-1で(左から) 探索していく	
        for (int pivotCol = 0; pivotCol < column && pivotRow < row; pivotCol++) {
        	
            // 1) pivot探索：pivotRow..m-1 のうち |a[r][pivotCol]| > eps の行
            int bestRow = findPivotRow(matrix, pivotRow, pivotCol, eps);
            if (bestRow < 0) {
                continue;
            }

            // 2) 行の交換：pivot行を pivotRow に持ってくる
            if (bestRow != pivotRow) {
                swapRows(matrix, bestRow, pivotRow);
            }

            // 3) pivot を 1 に正規化
            BigDecimal pivot = matrix[pivotRow][pivotCol];
            normalizeRow(matrix, pivotRow, pivot, mc);

            // 4) pivot列の他の行を 0 にする（上も下も）
            for (int r = 0; r < row; r++) {
                if (r == pivotRow) {
                    continue;
                }
                BigDecimal factor = matrix[r][pivotCol];
                if (isZero(factor, eps)) {
                    continue;
                }
                eliminate(matrix, r, pivotRow, factor, mc);
            }

            pivotRow++;
        }

        // 微小値を 0 に寄せる（見やすさ・判定の安定化）
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < column; c++) {
                if (isZero(matrix[r][c], eps)) {
                	matrix[r][c] = ZERO;
                }
            }
        }

        return Matrix.of(matrix);
    }

    /**
     * 一般の連立一次方程式 A x = b（Aはm×n）について、
     * 一意解が存在する場合のみ解ベクトル x（n×1）を返す。
     * <p>一意解の条件（RREF に基づく）:</p>
     * <ul>
     *   <li>rank(A) </li>
     *   <li>rank(A) = n（自由変数がない）</li>
     * </ul>
     * 
     * @param augmented 拡大係数行列 [A|b]
     * @param mc BigDecimal の除算/乗算/加減算に適用する MathContext
	 * @param eps 0判定用の許容誤差（|v| <= eps を 0 とみなす、0以上）
	 * @return 解ベクトル x（n×1） 
     */
    public static Matrix solveUnique(AugmentedMatrix augmented, MathContext mc, BigDecimal eps) {
    	Objects.requireNonNull(augmented, "augmented must not be null");
        Objects.requireNonNull(mc, "mc must not be null");
        Objects.requireNonNull(eps, "eps must not be null");
        if (eps.signum() < 0) {
            throw new IllegalArgumentException("eps must be non-negative: eps=" + eps);
        }

        Matrix rref = rref(augmented, mc, eps);

        int m = rref.rows();
        int nPlus1 = rref.cols();
        if (nPlus1 < 2) {
            throw new IllegalArgumentException("augmented matrix must have at least 2 columns: cols=" + nPlus1);
        }
        int n = nPlus1 - 1; // 係数行列の列数

        // 1) 矛盾行の検出： (0 0 0 ... | c) で c≠0 な行があれば解なし
        // 係数行列のランクと拡大係数行列のランクが一致しないと同値
        if (hasInconsistentRow(rref, eps)) {
            throw new IllegalArgumentException("No solution (inconsistent system).");
        }

        // 2) rank(A) (A：係数行列)を数える（左側 n 列）
        int rankA = rankOfCoefficientPart(rref, n, eps);

        // 3) rank(A) < n なら自由変数があるので無限解
        if (rankA < n) {
            throw new IllegalArgumentException("Infinite solutions (free variables exist).");
        }

        // 4) rank(A) == n なら一意解（m≠nでもOK）
        //    ピボット列→その行の右辺 を拾って x を構成する
        BigDecimal[][] x = new BigDecimal[n][1];
        for (int i = 0; i < n; i++) {
            x[i][0] = ZERO;
        }

        for (int r = 0; r < m; r++) {
            int pivotCol = findPivotCol(rref, r, n, eps);
            if (pivotCol >= 0) {
                x[pivotCol][0] = rref.get(r, n);
            }
        }

        return Matrix.of(x);
    }

    // ---- helpers ----

    private static int findPivotRow(BigDecimal[][] a, int startRow, int col, BigDecimal eps) {
        for (int r = startRow; r < a.length; r++) {
            if (!isZero(a[r][col], eps)) {
                return r;
            }
        }
        return -1;
    }

    private static void swapRows(BigDecimal[][] a, int r1, int r2) {
        BigDecimal[] tmp = a[r1];
        a[r1] = a[r2];
        a[r2] = tmp;
    }

    private static void normalizeRow(BigDecimal[][] a, int row, BigDecimal pivot, MathContext mc) {
        int cols = a[row].length;
        for (int c = 0; c < cols; c++) {
            a[row][c] = a[row][c].divide(pivot, mc);
        }
    }

    private static void eliminate(BigDecimal[][] a, int targetRow, int pivotRow, BigDecimal factor, MathContext mc) {
        int cols = a[targetRow].length;
        for (int c = 0; c < cols; c++) {
            BigDecimal sub = factor.multiply(a[pivotRow][c], mc);
            a[targetRow][c] = a[targetRow][c].subtract(sub, mc);
        }
    }

    private static boolean isZero(BigDecimal v, BigDecimal eps) {
        return v.abs().compareTo(eps) <= 0;
    }
    
    private static boolean hasInconsistentRow(Matrix rref, BigDecimal eps) {
        int m = rref.rows();
        int n = rref.cols() - 1; // 変数数
        for (int r = 0; r < m; r++) {
            boolean allZeroLeft = true;
            for (int c = 0; c < n; c++) {
                if (!isZero(rref.get(r, c), eps)) {
                    allZeroLeft = false;
                    break;
                }
            }
            if (allZeroLeft && !isZero(rref.get(r, n), eps)) {
                return true;
            }
        }
        return false;
    }

    private static int rankOfCoefficientPart(Matrix rref, int nVars, BigDecimal eps) {
        int rank = 0;
        for (int r = 0; r < rref.rows(); r++) {
            boolean nonZero = false;
            for (int c = 0; c < nVars; c++) {
                if (!isZero(rref.get(r, c), eps)) {
                    nonZero = true;
                    break;
                }
            }
            if (nonZero) {
                rank++;
            }
        }
        return rank;
    }

    /**
     * RREF の行 row におけるピボット列を返す（なければ -1）。
     * ピボット列 = その行で最初に現れる非ゼロ成分の列。
     */
    private static int findPivotCol(Matrix rref, int row, int nVars, BigDecimal eps) {
        for (int c = 0; c < nVars; c++) {
            if (!isZero(rref.get(row, c), eps)) {
                return c;
            }
        }
        return -1;
    }
}
