package com.example.grpc.client.grpcclient;

import java.util.concurrent.Callable;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc;

public class StubThreading implements Callable<Integer[][]> {
    private Integer[][] A;
    private Integer[][] B;
    private Integer[][] C;
    private int[][] Aint;
    private int[][] Bint;
    private int[][] Cint;
    private MatrixServiceGrpc.MatrixServiceBlockingStub stub;
    private int sizeOfProcess;

    public StubThreading(Integer[][] A, Integer[][] B, MatrixServiceGrpc.MatrixServiceBlockingStub stub, int sizeOfProcess) {
        this.A = A;
        this.B = B;
        this.C = new Integer[sizeOfProcess][sizeOfProcess];
        this.Aint = new int[sizeOfProcess][sizeOfProcess];
        this.Bint = new int[sizeOfProcess][sizeOfProcess];
        this.Cint = new int[sizeOfProcess][sizeOfProcess];
        this.stub = stub;
        this.sizeOfProcess = sizeOfProcess;
    }

    @Override
    public Integer[][] call() throws InterruptedException {

        Aint = MatrixProcessing.IntegerToInt(A, Aint, sizeOfProcess);
        Bint = MatrixProcessing.IntegerToInt(B, Bint, sizeOfProcess);

        Cint = MatrixProcessing.matrixMultiplicationFinal(Aint, Bint, stub);
        C = MatrixProcessing.IntToInteger(Cint, C, sizeOfProcess);
        
        return C;
    }
}
