package com.example.grpc.client.grpcclient;

import com.example.grpc.server.grpcserver.MatrixRequest;
import com.example.grpc.server.grpcserver.MatrixReply;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc;
import java.lang.Math;

public class MatrixProcessing {
    public static int[][][][][] splitMatrix(int[][][][][] result_sq, int[][] A, int[][] B, int matrixSize, int sizeOfProcess)
    {
        for (int i=0; i < (matrixSize / sizeOfProcess); i++) {
            for (int j=0; j < (matrixSize / sizeOfProcess); j++) {
                for (int m=0; m < sizeOfProcess; m++) {
                    for (int n=0; n < sizeOfProcess; n++) {
                        result_sq[0][i][j][m][n] = A[m+(i*sizeOfProcess)][n+(j*sizeOfProcess)];
                        result_sq[1][i][j][m][n] = B[m+(i*sizeOfProcess)][n+(j*sizeOfProcess)];
                    }
                }
            }
        }
        return result_sq;
    }

    public static int[][] assembleMatrix(int[][] C, int[][][][] Csub, int matrixSize, int sizeOfProcess)
    {
        for (int i=0; i < (matrixSize / sizeOfProcess); i++) {
            for (int j=0; j < (matrixSize / sizeOfProcess); j++) {
                for (int m=0; m < sizeOfProcess; m++) {
                    for (int n=0; n < sizeOfProcess; n++) {
                        C[m+(i*sizeOfProcess)][n+(j*sizeOfProcess)] = Csub[i][j][m][n];
                    }
                }
            }
        }
        return C;
    }

    public static void printSubMatrix(int[][] matrix, int sizeOfProcess)
    {
        for (int m=0; m < sizeOfProcess; m++) {
            for (int n=0; n < sizeOfProcess; n++) {
                System.out.print(matrix[m][n] + " ");
            }
            System.out.println();
        }
    }

    public static Integer[][] IntToInteger(int[][] intArray, Integer[][] IntegerArray, int sizeOfProcess)
    {
        for (int m=0; m < sizeOfProcess; m++) {
            for (int n=0; n < sizeOfProcess; n++) {
                IntegerArray[m][n] = intArray[m][n];
            }
        }
        return IntegerArray;
    }

    public static int[][] IntegerToInt(Integer[][] IntegerArray, int[][] intArray, int sizeOfProcess)
    {
        for (int m=0; m < sizeOfProcess; m++) {
            for (int n=0; n < sizeOfProcess; n++) {
                intArray[m][n] = IntegerArray[m][n];
            }
        }
        return intArray;
    }
    
    public static int[][][][] shiftRowLeft(int[][][][] array, int i, int numberOfSubSectionsSQRT)
    {
        int[][] temp = array[i][0];
        for (int k=0; k < numberOfSubSectionsSQRT-1; k++) {
            array[i][k] = array[i][k+1];
        }
        array[i][numberOfSubSectionsSQRT - 1] = temp;
        return array;
    }
 
    public static int[][][][] shiftRowUp(int[][][][] array, int i, int numberOfSubSectionsSQRT)
    {
        int[][] temp = array[0][i];
        for (int k=0; k < numberOfSubSectionsSQRT-1; k++) {
            array[k][i] = array[k+1][i];
        }
        array[numberOfSubSectionsSQRT-1][i] = temp;
        return array;
    }

    public static int[][] matrixMultiplicationFinal(int[][] A, int[][] B, MatrixServiceGrpc.MatrixServiceBlockingStub stub){
        
        MatrixReply MR_C = null;
        return  matrixMultiplication(
                A, B, 0, 0, 
                0,0, A.length, stub, MR_C);
    }

    private static int[][] matrixMultiplication(
            int[][] A, int[][] B, int rowA, int colA, int rowB, int colB, int size,
            MatrixServiceGrpc.MatrixServiceBlockingStub stub, MatrixReply MR_C)
    {

        int[][] C = new int[size][size];

        if(size==1) {
            MR_C = stub.multiplyBlock(MatrixRequest.newBuilder()
                    .setA00(A[rowA][colA])
                    .setB00(B[rowB][colB])
                    .build());
            C[0][0] = MR_C.getC00();
            //C[0][0]= A[rowA][colA]*B[rowB][colB];

        }
        else {
            int newSize= size/2;
            //C11
             sumMatrix(C, 
                matrixMultiplication(A, B, rowA, colA, rowB, colB, newSize, stub, MR_C),
                matrixMultiplication(A, B, rowA, colA+newSize, rowB+ newSize,
                    colB, newSize, stub, MR_C)
                    , 0, 0, stub, MR_C);
            //C12
             sumMatrix(C, 
                matrixMultiplication(A, B, rowA, colA, rowB, colB + newSize, newSize, stub, MR_C),
                matrixMultiplication(A, B, rowA, colA+newSize, rowB+ newSize,
                    colB+newSize, newSize, stub, MR_C)
                    , 0, newSize, stub, MR_C);
            //C21
             sumMatrix(C, 
                matrixMultiplication(A, B, rowA+ newSize, colA, rowB, colB, newSize, stub, MR_C),
                matrixMultiplication(A, B, rowA+ newSize, colA+newSize,
                    rowB+ newSize, colB, newSize, stub, MR_C)
                    , newSize, 0, stub, MR_C);
            //C22
             sumMatrix(C, 
                matrixMultiplication(A, B, rowA+ newSize, colA, rowB, colB+newSize, newSize, stub, MR_C),
                matrixMultiplication(A, B, rowA+ newSize, colA+newSize,
                    rowB+ newSize, colB+newSize, newSize, stub, MR_C)
                    , newSize, newSize, stub, MR_C);
        }
        return C;
    }

    private static void sumMatrix(int[][] C, int[][]A, int[][]B,int rowC, int colC, MatrixServiceGrpc.MatrixServiceBlockingStub stub, MatrixReply MR_C){
        int n=A.length;
        for(int i =0; i<n; i++){
            for(int j=0; j<n; j++){ 
                MR_C = stub.addBlock(MatrixRequest.newBuilder()
                        .setA00(A[i][j])
                        .setB00(B[i][j])
                        .build());
                C[i+rowC][j+colC] = MR_C.getC00();
                //C[i+rowC][j+colC]=A[i][j]+B[i][j];
            }
        }
    }
}

