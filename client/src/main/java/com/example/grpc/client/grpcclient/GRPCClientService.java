package com.example.grpc.client.grpcclient;

import com.example.grpc.server.grpcserver.MatrixServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import java.lang.Math;
import java.util.concurrent.*;

@Service
public class GRPCClientService {
    public int[][] matrixMult(int[][] inputA, int[][] inputB)
    {
        ManagedChannel[] channel = new ManagedChannel[8];
        MatrixServiceGrpc.MatrixServiceBlockingStub[] stub = new MatrixServiceGrpc.MatrixServiceBlockingStub[8];
        String[] address = new String[8];
        address[0] = "172.31.46.42";
        address[1] = "172.31.21.20";
        address[2] = "172.31.59.119";
        address[3] = "172.31.52.191";
        address[4] = "172.31.57.20";
        address[5] = "172.31.24.57";
        address[6] = "172.31.30.254";
        address[7] = "172.31.25.98";

        for (int i=0; i < address.length; i++) {
            channel[i] = ManagedChannelBuilder.forAddress(address[i], 9090).usePlaintext().build();
            stub[i] = MatrixServiceGrpc.newBlockingStub(channel[i]);
        }

        int matrixSize = inputA.length;
        int[][] A = new int[matrixSize][matrixSize];
        int[][] B = new int[matrixSize][matrixSize];
        int[][] C = new int[matrixSize][matrixSize];

        A = inputA;
        B = inputB;

        int bound1 = 0;
        int bound2 = 0;
        if (matrixSize == 4) {
            bound2 = 1000;
            bound1 = 800;
        }
        else if (matrixSize == 16) {
            bound2 = 4000;
            bound1 = 2900;
        }
        else if (matrixSize == 32) {
            bound2 = 26000;
            bound1 = 22000;
        }
        else if (matrixSize == 64) {
            bound2 = 210000;
            bound1 = 135000;
        }

        int numberOfProcesses = 16;
        int numberOfServers = 8;

        int sizeOfProcess = matrixSize / (int)Math.sqrt(numberOfProcesses);
        int numberOfSubSections = (matrixSize / sizeOfProcess) * (matrixSize / sizeOfProcess);
        int numberOfSubSectionsSQRT = (int)Math.sqrt(numberOfSubSections);

        int[][][][][] result_sq = new int[2][numberOfSubSectionsSQRT][numberOfSubSectionsSQRT][sizeOfProcess][sizeOfProcess];
        int[][][][] Asub = new int[numberOfSubSectionsSQRT][numberOfSubSectionsSQRT][sizeOfProcess][sizeOfProcess];
        int[][][][] Bsub = new int[numberOfSubSectionsSQRT][numberOfSubSectionsSQRT][sizeOfProcess][sizeOfProcess];
        int[][][][] Csub = new int[numberOfSubSectionsSQRT][numberOfSubSectionsSQRT][sizeOfProcess][sizeOfProcess];

        result_sq = MatrixProcessing.splitMatrix(result_sq, A, B, matrixSize, sizeOfProcess);
        Asub = result_sq[0];
        Bsub = result_sq[1];

        System.out.println("\nThe input matrix is "+matrixSize+" by "+matrixSize+". There are "+numberOfProcesses+" servers, each one processes a sub-matrix of size "+sizeOfProcess+" by "+sizeOfProcess+" and there are "+numberOfSubSections+" divisions of the input matrix in total.");

        try {
            C = parallelMatrixMult(C, Asub, Bsub, Csub, numberOfProcesses, sizeOfProcess,
                    numberOfSubSectionsSQRT, matrixSize, stub, numberOfServers);
        } catch(InterruptedException e) {
            System.out.println(e);
        } catch(ExecutionException e) {
            System.out.println(e);
        }

        for (int i=0; i < address.length; i++) {
            channel[i].shutdown();
        }
        return C;
    }

    private static int[][] parallelMatrixMult(int[][] C, int[][][][] Asub, int[][][][] Bsub,
            int[][][][] Csub, int numberOfProcesses, int sizeOfProcess, int numberOfSubSectionsSQRT,
            int matrixSize, MatrixServiceGrpc.MatrixServiceBlockingStub[] stub, int numberOfServers) throws InterruptedException, ExecutionException
    {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        int numberOfSteps = (int)Math.sqrt(numberOfProcesses);
         
        for (int i=0; i < numberOfSteps; i++) {
            if (i==0) {
                if (numberOfSteps == 4) {
                    Asub = MatrixProcessing.shiftRowLeft(Asub, 1, numberOfSubSectionsSQRT);
                    Asub = MatrixProcessing.shiftRowLeft(Asub, 2, numberOfSubSectionsSQRT);
                    Asub = MatrixProcessing.shiftRowLeft(Asub, 2, numberOfSubSectionsSQRT);
                    Asub = MatrixProcessing.shiftRowLeft(Asub, 3, numberOfSubSectionsSQRT);
                    Asub = MatrixProcessing.shiftRowLeft(Asub, 3, numberOfSubSectionsSQRT);
                    Asub = MatrixProcessing.shiftRowLeft(Asub, 3, numberOfSubSectionsSQRT);

                    Bsub = MatrixProcessing.shiftRowUp(Bsub, 1, numberOfSubSectionsSQRT);
                    Bsub = MatrixProcessing.shiftRowUp(Bsub, 2, numberOfSubSectionsSQRT);
                    Bsub = MatrixProcessing.shiftRowUp(Bsub, 2, numberOfSubSectionsSQRT);
                    Bsub = MatrixProcessing.shiftRowUp(Bsub, 3, numberOfSubSectionsSQRT);
                    Bsub = MatrixProcessing.shiftRowUp(Bsub, 3, numberOfSubSectionsSQRT);
                    Bsub = MatrixProcessing.shiftRowUp(Bsub, 3, numberOfSubSectionsSQRT);
                }
                else {
                    Asub = MatrixProcessing.shiftRowLeft(Asub, 1, numberOfSubSectionsSQRT);

                    Bsub = MatrixProcessing.shiftRowUp(Bsub, 1, numberOfSubSectionsSQRT);
                }
            }
            else {
                for (int a=0; a < numberOfSubSectionsSQRT; a++){
                    Asub = MatrixProcessing.shiftRowLeft(Asub, a, numberOfSubSectionsSQRT);

                    Bsub = MatrixProcessing.shiftRowUp(Bsub, a, numberOfSubSectionsSQRT);
                }
            }

            Integer[][][][] AsubArray = new Integer[numberOfSteps][numberOfSteps][sizeOfProcess][sizeOfProcess];
            Integer[][][][] BsubArray = new Integer[numberOfSteps][numberOfSteps][sizeOfProcess][sizeOfProcess];
            Integer[][][][] outputArray = new Integer[numberOfSteps][numberOfSteps][sizeOfProcess][sizeOfProcess];
            int[][][][] result = new int [numberOfSteps][numberOfSteps]
                [sizeOfProcess][sizeOfProcess];

            int stubCount;
            for (int m=0; m < numberOfSteps; m++) {
                for (int n=0; n < numberOfSteps; n++) {
                    AsubArray[m][n] = MatrixProcessing.IntToInteger(Asub[m][n], AsubArray[m][n], sizeOfProcess);
                    BsubArray[m][n] = MatrixProcessing.IntToInteger(Bsub[m][n], BsubArray[m][n], sizeOfProcess);
                }
            }

            Future<Integer[][]> futureArray0 = null;
            Future<Integer[][]> futureArray1 = null;
            Future<Integer[][]> futureArray2 = null;
            Future<Integer[][]> futureArray3 = null;
            Future<Integer[][]> futureArray4 = null;
            Future<Integer[][]> futureArray5 = null;
            Future<Integer[][]> futureArray6 = null;
            Future<Integer[][]> futureArray7 = null;

            futureArray0 = executorService.submit(new StubThreading(AsubArray[0][0], BsubArray[0][0], stub[0], sizeOfProcess));
            futureArray1 = executorService.submit(new StubThreading(AsubArray[0][1], BsubArray[0][1], stub[1], sizeOfProcess));
            futureArray2 = executorService.submit(new StubThreading(AsubArray[0][2], BsubArray[0][2], stub[2], sizeOfProcess));
            futureArray3 = executorService.submit(new StubThreading(AsubArray[0][3], BsubArray[0][3], stub[3], sizeOfProcess));
            futureArray4 = executorService.submit(new StubThreading(AsubArray[1][0], BsubArray[1][0], stub[4], sizeOfProcess));
            futureArray5 = executorService.submit(new StubThreading(AsubArray[1][1], BsubArray[1][1], stub[5], sizeOfProcess));
            futureArray6 = executorService.submit(new StubThreading(AsubArray[1][2], BsubArray[1][2], stub[6], sizeOfProcess));
            futureArray7 = executorService.submit(new StubThreading(AsubArray[1][3], BsubArray[1][3], stub[7], sizeOfProcess));
            outputArray[0][0] = futureArray0.get();
            outputArray[0][1] = futureArray1.get();
            outputArray[0][2] = futureArray2.get();
            outputArray[0][3] = futureArray3.get();
            outputArray[1][0] = futureArray4.get();
            outputArray[1][1] = futureArray5.get();
            outputArray[1][2] = futureArray6.get();
            outputArray[1][3] = futureArray7.get();

            futureArray0 = executorService.submit(new StubThreading(AsubArray[2][0], BsubArray[2][0], stub[0], sizeOfProcess));
            futureArray1 = executorService.submit(new StubThreading(AsubArray[2][1], BsubArray[2][1], stub[1], sizeOfProcess));
            futureArray2 = executorService.submit(new StubThreading(AsubArray[2][2], BsubArray[2][2], stub[2], sizeOfProcess));
            futureArray3 = executorService.submit(new StubThreading(AsubArray[2][3], BsubArray[2][3], stub[3], sizeOfProcess));
            futureArray4 = executorService.submit(new StubThreading(AsubArray[3][0], BsubArray[3][0], stub[4], sizeOfProcess));
            futureArray5 = executorService.submit(new StubThreading(AsubArray[3][1], BsubArray[3][1], stub[5], sizeOfProcess));
            futureArray6 = executorService.submit(new StubThreading(AsubArray[3][2], BsubArray[3][2], stub[6], sizeOfProcess));
            futureArray7 = executorService.submit(new StubThreading(AsubArray[3][3], BsubArray[3][3], stub[7], sizeOfProcess));
            outputArray[2][0] = futureArray0.get();
            outputArray[2][1] = futureArray1.get();
            outputArray[2][2] = futureArray2.get();
            outputArray[2][3] = futureArray3.get();
            outputArray[3][0] = futureArray4.get();
            outputArray[3][1] = futureArray5.get();
            outputArray[3][2] = futureArray6.get();
            outputArray[3][3] = futureArray7.get();

            stubCount = 0;
            for (int m=0; m < numberOfSteps; m++) {
                for (int n=0; n < numberOfSteps; n++) {
                    result[m][n] = MatrixProcessing.IntegerToInt(outputArray[m][n], result[m][n], sizeOfProcess);
                    stubCount++;
                }
            }
            
            stubCount = 0;
            for (int m=0; m < numberOfSteps; m++) {
                for (int n=0; n < numberOfSteps; n++) {
                    if (m==0 && n<=1) {
                        stubCount = 0;
                    }
                    else {
                        if (n%2 == 0) {
                            stubCount++;
                        }
                    }
                    for (int x=0; x < sizeOfProcess; x++) {
                        for (int y=0; y < sizeOfProcess; y++) {
                            Csub[m][n][x][y] += result[m][n][x][y];
                        }
                    } 
                }
            }
        }
        executorService.shutdown();
        C = MatrixProcessing.assembleMatrix(C, Csub, matrixSize, sizeOfProcess);
        return C;
    }
}

