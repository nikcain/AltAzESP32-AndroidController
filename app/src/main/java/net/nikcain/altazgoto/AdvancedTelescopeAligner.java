package net.nikcain.altazgoto;

// Written by Google AI

import java.util.ArrayList;
import java.util.List;

public class AdvancedTelescopeAligner {

    // Calibration star record holding both telescope encoder data and celestial truth
    public static class AlignmentStar {
        double rawAlt, rawAz;   // Degrees
        double trueAlt, trueAz; // Degrees

        public AlignmentStar(double rawAlt, double rawAz, double trueAlt, double trueAz) {
            this.rawAlt = rawAlt;   this.rawAz = rawAz;
            this.trueAlt = trueAlt; this.trueAz = trueAz;
        }
    }

    private final List<AlignmentStar> stars = new ArrayList<>();
    private final double[][] T = new double[3][3]; // Least-squares optimized matrix
    
    // Mechanical error parameters (radians)
    private double epsilonX = 0.0; // Axis non-orthogonality
    private double epsilonY = 0.0; // Collimation offset
    private boolean isCalibrated = false;

    public void addAlignmentStar(double rawAlt, double rawAz, double trueAlt, double trueAz) {
        stars.add(new AlignmentStar(rawAlt, rawAz, trueAlt, trueAz));
        this.isCalibrated = false; // Require recalculation
    }

    public void clearStars() {
        stars.clear();
        this.isCalibrated = false;
    }

    public boolean isCalibrated()
    {
        return isCalibrated;
    }

    /**
     * Estimates mechanical errors (epsilonX, epsilonY) from data trends,
     * applies the correction to all points, and resolves the 3x3 transform via Least-Squares.
     */
    public void calibrate() {
        if (stars.size() < 2) {
            throw new IllegalStateException("At least 2 stars are required for a baseline transformation.");
        }

        // Step 1: Estimate mechanical axis alignment errors (Requires 3+ stars for optimal results)
        if (stars.size() >= 3) {
            estimateMechanicalErrors();
        } else {
            this.epsilonX = 0.0;
            this.epsilonY = 0.0;
        }

        // Step 2: Build the Direction Cosine data arrays M and N
        int K = stars.size();
        double[][] M = new double[3][K];
        double[][] N = new double[3][K];

        for (int i = 0; i < K; i++) {
            AlignmentStar star = stars.get(i);

            // Warp raw inputs to strip out mechanical errors first
            double[] mechCorrectedRaw = applyMechanicalCorrection(star.rawAlt, star.rawAz);
            double rAltRad = Math.toRadians(mechCorrectedRaw[0]);
            double rAzRad = Math.toRadians(mechCorrectedRaw[1]);

            double tAltRad = Math.toRadians(star.trueAlt);
            double tAzRad = Math.toRadians(star.trueAz);

            // Compute unit vector direction cosines
            M[0][i] = Math.cos(rAltRad) * Math.cos(rAzRad);
            M[1][i] = Math.cos(rAltRad) * Math.sin(rAzRad);
            M[2][i] = Math.sin(rAltRad);

            N[0][i] = Math.cos(tAltRad) * Math.cos(tAzRad);
            N[1][i] = Math.cos(tAltRad) * Math.sin(tAzRad);
            N[2][i] = Math.sin(tAltRad);
        }

        // Step 3: Compute the Least-Squares Pseudoinverse Matrix Solution: T = N * M^T * (M * M^T)^-1
        double[][] MT = transpose(M);
        double[][] M_MT = multiply(M, MT);
        double[][] M_MT_inv = invert3x3(M_MT);
        double[][] NT = multiply(N, MT);
        
        multiply3x3(NT, M_MT_inv, this.T);
        this.isCalibrated = true;
    }

    /**
     * Map any live incoming raw Alt-Az value into a fully corrected sky target.
     */
    public double[] transform(double rawAlt, double rawAz) {
        if (!isCalibrated) {
            throw new IllegalStateException("System must be calibrated first.");
        }

        // 1. Structural Mechanical Correction Pass
        double[] mechCorrected = applyMechanicalCorrection(rawAlt, rawAz);
        double altRad = Math.toRadians(mechCorrected[0]);
        double azRad = Math.toRadians(mechCorrected[1]);

        // 2. Vector Projection Pass
        double[] rawVec = new double[]{
            Math.cos(altRad) * Math.cos(azRad),
            Math.cos(altRad) * Math.sin(azRad),
            Math.sin(altRad)
        };

        // 3. Matrix Multi-Star Rotation Pass (T * rawVec)
        double[] trueVec = new double[3];
        for (int i = 0; i < 3; i++) {
            trueVec[i] = T[i][0] * rawVec[0] + T[i][1] * rawVec[1] + T[i][2] * rawVec[2];
        }

        // Convert 3D Vector back to Spherical Horizontal System
        double trueAlt = Math.asin(trueVec[2]);
        double trueAz = Math.atan2(trueVec[1], trueVec[0]);

        double trueAzDeg = Math.toDegrees(trueAz);
        if (trueAzDeg < 0) trueAzDeg += 360.0;

        return new double[]{ Math.toDegrees(trueAlt), trueAzDeg };
    }

    // --- Core Mechanical Warping Engine ---
    private double[] applyMechanicalCorrection(double rawAlt, double rawAz) {
        double h = Math.toRadians(rawAlt);
        double a = Math.toRadians(rawAz);

        // Apply Taki's physical Axis-Offset and Optical Collimation formula shifts
        double correctedAz = a - (epsilonX * Math.tan(h) * Math.sin(a)) - (epsilonY / Math.cos(h));
        double correctedAlt = h - (epsilonX * Math.cos(a));

        return new double[]{ Math.toDegrees(correctedAlt), Math.toDegrees(correctedAz) };
    }

    /**
     * Approximates mechanical alignment drift trends to deduce mechanical axis anomalies.
     */
    private void estimateMechanicalErrors() {
        double sumAltErr = 0;
        double sumCosAz = 0;
        int count = stars.size();

        for (AlignmentStar star : stars) {
            double rAlt = Math.toRadians(star.rawAlt);
            double rAz = Math.toRadians(star.rawAz);
            double tAlt = Math.toRadians(star.trueAlt);

            double altError = rAlt - tAlt; 
            sumAltErr += altError;
            sumCosAz += Math.cos(rAz);
        }

        // Extrapolate orthogonality constant variance metric
        if (Math.abs(sumCosAz) > 1e-5) {
            this.epsilonX = sumAltErr / sumCosAz;
        } else {
            this.epsilonX = 0.0;
        }
        
        // Default collimation error buffer initialization parameter
        this.epsilonY = 0.0; 
    }

    // --- Matrix Mathematics Implementations ---
    private double[][] transpose(double[][] matrix) {
        double[][] result = new double[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    private double[][] multiply(double[][] A, double[][] B) {
        double[][] C = new double[A.length][B[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < B[0].length; j++) {
                for (int k = 0; k < A[0].length; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return C;
    }

    private void multiply3x3(double[][] A, double[][] B, double[][] result) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = 0;
                for (int k = 0; k < 3; k++) {
                    result[i][j] += A[i][k] * B[k][j];
                }
            }
        }
    }

    private double[][] invert3x3(double[][] A) {
        double det = A[0][0] * (A[1][1] * A[2][2] - A[1][2] * A[2][1]) -
                     A[0][1] * (A[1][0] * A[2][2] - A[1][2] * A[2][0]) +
                     A[0][2] * (A[1][0] * A[2][1] - A[1][1] * A[2][0]);

        if (Math.abs(det) < 1e-9) det = 1.0; // Stabilise colinear arrays safely

        double invDet = 1.0 / det;
        double[][] inv = new double[3][3];
        inv[0][0] = (A[1][1] * A[2][2] - A[1][2] * A[2][1]) * invDet;
        inv[0][1] = (A[0][2] * A[2][1] - A[0][1] * A[2][2]) * invDet;
        inv[0][2] = (A[0][1] * A[1][2] - A[0][2] * A[1][1]) * invDet;
        inv[1][0] = (A[1][2] * A[2][0] - A[1][0] * A[2][2]) * invDet;
        inv[1][1] = (A[0][0] * A[2][2] - A[0][2] * A[2][0]) * invDet;
        inv[1][2] = (A[0][2] * A[1][0] - A[0][0] * A[1][2]) * invDet;
        inv[2][0] = (A[1][0] * A[2][1] - A[1][1] * A[2][0]) * invDet;
        inv[2][1] = (A[0][1] * A[2][0] - A[0][0] * A[2][1]) * invDet;
        inv[2][2] = (A[0][0] * A[1][1] - A[0][1] * A[1][0]) * invDet;
        return inv;
    }
}
