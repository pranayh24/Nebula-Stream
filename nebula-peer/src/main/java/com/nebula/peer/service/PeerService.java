package com.nebula.peer.service;

import com.google.protobuf.ByteString;
import com.nebula.grpc.*;
import com.nebula.peer.dto.FileMetadata;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PeerService extends PeerServiceGrpc.PeerServiceImplBase {

    private final FileService fileService;

    public PeerService(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public void getManifest(ContentRequest request, StreamObserver<Manifest> responseObserver) {
        String hash = request.getContentHash();
        System.out.println("Received Manifest Request for: " + hash);

        try {
            FileMetadata meta = fileService.getFileMetadata(hash);
            if (meta == null) {
                responseObserver.onError(new RuntimeException("File not found"));
                return;
            }

            Manifest manifest = Manifest.newBuilder()
                    .setContentHash(meta.contentHash())
                    .setTotalChunks(meta.totalChunks())
                    .setTotalSize(meta.totalSize())
                    .setFileName("Unknown")
                    .build();

            responseObserver.onNext(manifest);
            responseObserver.onCompleted();
        } catch (IOException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public StreamObserver<ChunkRequest> downloadChunk(StreamObserver<ChunkData> responseObserver) {
        // return a request observer that handles the incoming stream of requests from the client
        return new StreamObserver<ChunkRequest>() {
            @Override
            public void onNext(ChunkRequest request) {
                try {
                    // read the requested chunk from disk
                    byte[] data = fileService.getChunk(request.getContentHash(), request.getChunkIndex());

                    // build the response
                    ChunkData response = ChunkData.newBuilder()
                            .setChunkIndex(request.getChunkIndex())
                            .setData(ByteString.copyFrom(data))
                            .build();

                    // send it back
                    responseObserver.onNext(response);
                    System.out.println("Served chunk: " + request.getChunkIndex());

                } catch (IOException e) {
                    System.err.println("Error reading chunk: " + e.getMessage());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Client cancelled or error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Client finished downloading.");
                responseObserver.onCompleted();
            }
        };
    }
}
