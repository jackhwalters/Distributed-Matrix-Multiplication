package com.example.grpc.client.grpcclient;

class MatrixTooSmallException extends RuntimeException {
    MatrixTooSmallException() {
        super("Matrix is too small\n");
    }
}
