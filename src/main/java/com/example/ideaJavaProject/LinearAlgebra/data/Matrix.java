package com.example.ideaJavaProject.LinearAlgebra.data;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Objects;

/**
 * 行列クラス
 */
public final class Matrix {
	
	/** 行 */
	private final int rows;
	/** 列 */
    private final int cols;
    
    /** 成分一覧 */
    private final BigDecimal[] data;
    

    /**
     * コンストラクタ
     * @param rows
     * @param cols
     * @param data
     */
    private Matrix(int rows, int cols, BigDecimal[] data) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("rows and cols must be positive: rows=" + rows + ", cols=" + cols);
        }
        this.rows = rows;
        this.cols = cols;
        this.data = Objects.requireNonNull(data, "data must not be null");
        
        // 成分の数が行 × 列か
        if (data.length != rows * cols) {
            throw new IllegalArgumentException(
                    "data length mismatch: expected=" + (rows * cols) + ", actual=" + data.length);
        }
        // 成分の値にnullが含まれていないか
        for (int i = 0; i < data.length; i++) {
            if (data[i] == null) {
                throw new IllegalArgumentException("data[" + i + "] must not be null");
            }
        }
    }
    
    /**
     * 行列の生成
     * @param values
     * @return
     */
    public static Matrix of(BigDecimal[][] values) {
    	
    	//行数チェック
        Objects.requireNonNull(values, "values must not be null");
        if (values.length == 0) {
            throw new IllegalArgumentException("values must have at least 1 row");
        }
        int rows = values.length;
        
        //列数チェック
        int cols = Objects.requireNonNull(values[0], "values[0] must not be null").length;
        if (cols == 0) {
            throw new IllegalArgumentException("values must have at least 1 column");
        }
        
        //ギザギザ配列チェック
        for (int r = 0; r < rows; r++) {
            BigDecimal[] row = Objects.requireNonNull(values[r], "values[" + r + "] must not be null");
            if (row.length != cols) {
                throw new IllegalArgumentException(
                        "values must be rectangular: row 0 cols=" + cols + ", row " + r + " cols=" + row.length);
            }
        }
        
        // 要素を設定
        BigDecimal[] data = new BigDecimal[rows * cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                BigDecimal v = values[r][c];
                if (v == null) {
                    throw new IllegalArgumentException("values[" + r + "][" + c + "] must not be null");
                }
                data[r * cols + c] = v;
            }
        }
        return new Matrix(rows, cols, data);
    }

    /**
     * 零行列の生成
     * @param rows 行
     * @param cols 列
     * @return 零行列
     */
    public static Matrix zeros(int rows, int cols) {
        BigDecimal[] data = new BigDecimal[rows * cols];
        Arrays.fill(data, BigDecimal.ZERO);
        return new Matrix(rows, cols, data);
    }

    /**
     * 単位行列の生成
     * @param n
     * @return 単位行列
     */
    public static Matrix identity(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive: n=" + n);
        }
        BigDecimal[] data = new BigDecimal[n * n];
        Arrays.fill(data, BigDecimal.ZERO);
        for (int i = 0; i < n; i++) {
            data[i * n + i] = BigDecimal.ONE;
        }
        return new Matrix(n, n, data);
    }

    /**
     * 行数の取得
     * @return 行数
     */
    public int rows() {
        return rows;
    }

    /**
     * 列数の取得
     * @return 列数
     */
    public int cols() {
        return cols;
    }

    /**
     * 指定の成分の値を取得
     * @param row
     * @param col
     * @return
     */
    public BigDecimal get(int row, int col) {
        checkIndex(row, col);
        return data[row * cols + col];
    }

    /**
     * 指定要素を変更して生成
     * @param row 指定行
     * @param col 指定列
     * @param value 変更した値
     * @return 変更後の行列
     */
    public Matrix with(int row, int col, BigDecimal value) {
        checkIndex(row, col);
        Objects.requireNonNull(value, "value must not be null");
        BigDecimal[] copied = Arrays.copyOf(data, data.length);
        copied[row * cols + col] = value;
        return new Matrix(rows, cols, copied);
    }

    /**
     * 行列の加算
     * @param other
     * @return
     */
    public Matrix add(Matrix other) {
        Objects.requireNonNull(other, "other must not be null");
        requireSameShape(other, "add");
        BigDecimal[] out = new BigDecimal[data.length];
        for (int i = 0; i < data.length; i++) {
            out[i] = this.data[i].add(other.data[i]);
        }
        return new Matrix(rows, cols, out);
    }

    /**
     * 行列の減算
     * @param other
     * @return
     */
    public Matrix subtract(Matrix other) {
        Objects.requireNonNull(other, "other must not be null");
        requireSameShape(other, "subtract");
        BigDecimal[] out = new BigDecimal[data.length];
        for (int i = 0; i < data.length; i++) {
            out[i] = this.data[i].subtract(other.data[i]);
        }
        return new Matrix(rows, cols, out);
    }

    /**
     * 行列の各成分をスカラー倍する
     * @param scalar
     * @param mc
     * @return
     */
    public Matrix scale(BigDecimal scalar, MathContext mc) {
        Objects.requireNonNull(scalar, "scalar must not be null");
        Objects.requireNonNull(mc, "mc must not be null");
        BigDecimal[] out = new BigDecimal[data.length];
        for (int i = 0; i < data.length; i++) {
            out[i] = this.data[i].multiply(scalar, mc);
        }
        return new Matrix(rows, cols, out);
    }

    /**
     * 行列の積
     * Matrix multiplication: A(m x n) * B(n x p) => C(m x p)
     */
    public Matrix multiply(Matrix other, MathContext mc) {
        Objects.requireNonNull(other, "other must not be null");
        Objects.requireNonNull(mc, "mc must not be null");
        
        // 次元チェック： (m×n) * (n×p) のみ定義可能（A.cols == B.rows）
        if (this.cols != other.rows) {
            throw new IllegalArgumentException(
                    "multiply shape mismatch: left=" + shape() + ", right=" + other.shape());
        }

        // 行列サイズ
        // A: m×n, B: n×p, C: m×p
        int m = this.rows;
        int n = this.cols;  // == other.rows
        int p = other.cols;

        // 各要素の累積和を 0 で初期化
        BigDecimal[] out = new BigDecimal[m * p];
        Arrays.fill(out, BigDecimal.ZERO);

        for (int i = 0; i < m; i++) {
            int leftRowBase = i * this.cols;
            int outRowBase = i * p;

            for (int k = 0; k < n; k++) {
            	
            	// A[i,k] を取り出す
                BigDecimal a = this.data[leftRowBase + k];
                
                // A[i,k] が 0 なら、その項 a * B[k,j] は常に0なので内側ループを省略
                if (a.signum() == 0) {
                    continue; // small optimization: skip zero
                }
                int rightRowBase = k * p;

                for (int j = 0; j < p; j++) {
                	// B[k,j] は rightRowBase + j
                    BigDecimal b = other.data[rightRowBase + j];
                    
                    // prod = A[i,k] * B[k,j]
                    BigDecimal prod = a.multiply(b, mc);
                    int outIndex = outRowBase + j;
                    // C[i,j] += prod
                    out[outRowBase + j] = out[outIndex].add(prod, mc);
                }
            }
        }
        return new Matrix(m, p, out);
    }

    /**
     * 転置行列の取得
     * @return 転置行列
     */
    public Matrix transpose() {
        BigDecimal[] out = new BigDecimal[rows * cols];
        for (int r = 0; r < rows; r++) {
            int rowBase = r * cols;
            for (int c = 0; c < cols; c++) {
                out[c * rows + r] = data[rowBase + c];
            }
        }
        return new Matrix(cols, rows, out);
    }

    /**
     * 行列の内容を 2 次元配列（BigDecimal[][]）として返す
     */
    public BigDecimal[][] toArray() {
        BigDecimal[][] out = new BigDecimal[rows][cols];
        for (int r = 0; r < rows; r++) {
            System.arraycopy(data, r * cols, out[r], 0, cols);
        }
        return out;
    }

    /**
     * 許容誤差 eps を用いた要素ごとの近似等価判定
     */
    public boolean equalsEps(Matrix other, BigDecimal eps) {
        Objects.requireNonNull(other, "other must not be null");
        Objects.requireNonNull(eps, "eps must not be null");
        if (eps.signum() < 0) {
            throw new IllegalArgumentException("eps must be non-negative: eps=" + eps);
        }
        if (this.rows != other.rows || this.cols != other.cols) {
            return false;
        }
        for (int i = 0; i < data.length; i++) {
            BigDecimal diff = this.data[i].subtract(other.data[i]).abs();
            if (diff.compareTo(eps) > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BigDecimalMatrix").append(shape()).append('\n');
        for (int r = 0; r < rows; r++) {
            sb.append('[');
            for (int c = 0; c < cols; c++) {
                if (c > 0) sb.append(", ");
                sb.append(get(r, c));
            }
            sb.append(']').append('\n');
        }
        return sb.toString();
    }

    // ---- helpers ----

    /**
     * 同じ行数、列数かのチェック
     * @param other
     * @param op
     */
    private void requireSameShape(Matrix other, String op) {
        if (this.rows != other.rows || this.cols != other.cols) {
            throw new IllegalArgumentException(op + " shape mismatch: left=" + shape() + ", right=" + other.shape());
        }
    }

    /**
     * 
     * @param row
     * @param col
     */
    private void checkIndex(int row, int col) {
        if (row < 0 || row >= rows) {
            throw new IndexOutOfBoundsException("row out of range: row=" + row + ", rows=" + rows);
        }
        if (col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException("col out of range: col=" + col + ", cols=" + cols);
        }
    }

    private String shape() {
        return "(" + rows + "x" + cols + ")";
    }

}
