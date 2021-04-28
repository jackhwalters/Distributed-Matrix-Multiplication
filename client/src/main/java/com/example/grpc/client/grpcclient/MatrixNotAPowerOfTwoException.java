package com.example.grpc.client.grpcclient;

class MatrixNotAPowerOfTwoException extends RuntimeException {
    MatrixNotAPowerOfTwoException() {
        super("Matrix is not a power of two\n");
    }
}
