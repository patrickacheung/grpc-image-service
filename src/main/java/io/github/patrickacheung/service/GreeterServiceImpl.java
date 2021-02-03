package io.github.patrickacheung.service;

import io.github.patrickacheung.HelloReply;
import io.github.patrickacheung.HelloRequest;
import io.github.patrickacheung.GreeterServiceGrpc;
import io.grpc.stub.StreamObserver;

public class GreeterServiceImpl extends GreeterServiceGrpc.GreeterServiceImplBase {
    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
