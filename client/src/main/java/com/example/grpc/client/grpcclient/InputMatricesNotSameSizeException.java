package com.example.grpc.client.grpcclient;

class InputMatricesNotSameSizeException extends RuntimeException {
    InputMatricesNotSameSizeException() {
        super("The two matrices are not the same size\n");
    }
}
