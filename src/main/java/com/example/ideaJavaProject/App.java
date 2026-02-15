package com.example.ideaJavaProject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import com.example.ideaJavaProject.LinearAlgebra.data.AugmentedMatrix;
import com.example.ideaJavaProject.LinearAlgebra.data.CoefficientMatrix;
import com.example.ideaJavaProject.LinearAlgebra.data.ConstantVector;
import com.example.ideaJavaProject.LinearAlgebra.data.Matrix;
import com.example.ideaJavaProject.LinearAlgebra.service.GaussJordanService;

/**
 * Hello world!
 */
public class App {
	public static void main(String[] args) {
		Matrix aRaw = Matrix.of(new BigDecimal[][] {
				{ new BigDecimal("2"), new BigDecimal("3") },
				{ new BigDecimal("1"), new BigDecimal("2") }
		});

		Matrix bRaw = Matrix.of(new BigDecimal[][] {
				{ new BigDecimal("4") },
				{ new BigDecimal("3") }
		});

		CoefficientMatrix a = new CoefficientMatrix(aRaw);
		ConstantVector b = new ConstantVector(bRaw);
		AugmentedMatrix ab = AugmentedMatrix.of(a, b);

		MathContext mc = new MathContext(30, RoundingMode.HALF_UP);
		BigDecimal eps = new BigDecimal("0"); // BigDecimal中心なら0でもOK。丸め誤差対策なら 1E-25 等

		Matrix rref = GaussJordanService.rref(ab, mc, eps);
		System.out.println("RREF:\n" + rref);
		
		Matrix x = GaussJordanService.solveUnique(ab, mc, eps);
        System.out.println("Solution x:\n" + x);
	}
}
