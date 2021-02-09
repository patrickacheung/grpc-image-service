package io.github.patrickacheung.service;

import io.github.patrickacheung.NLImage;
import io.github.patrickacheung.NLImageRotateRequest;
import io.github.patrickacheung.NLImageServiceGrpc;
import io.grpc.stub.StreamObserver;

public class ImageServiceImpl extends NLImageServiceGrpc.NLImageServiceImplBase {
    @Override
    public void rotateImage(NLImageRotateRequest req, StreamObserver<NLImage> responseObserver) {
        NLImage reply = req.getImage();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
