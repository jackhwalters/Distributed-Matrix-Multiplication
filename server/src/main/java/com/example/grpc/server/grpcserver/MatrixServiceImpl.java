package com.example.grpc.server.grpcserver;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class MatrixServiceImpl extends MatrixServiceGrpc.MatrixServiceImplBase {
    @Override
    public void addBlock(MatrixRequest request, StreamObserver<MatrixReply> reply)
    {
	System.out.println("addBlock request received");
        int C00 = request.getA00() + request.getB00();
        MatrixReply response = MatrixReply.newBuilder().setC00(C00).build();
        reply.onNext(response);
        reply.onCompleted();
    }

    @Override
    public void multiplyBlock(MatrixRequest request, StreamObserver<MatrixReply> reply)
    {
	System.out.println("multiplyBlock request received");
        int C00 = request.getA00() * request.getB00();
        MatrixReply response = MatrixReply.newBuilder().setC00(C00).build();
        reply.onNext(response);
        reply.onCompleted();
    }
}

