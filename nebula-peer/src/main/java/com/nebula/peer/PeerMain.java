package com.nebula.peer;

import com.nebula.grpc.ChunkData;
import com.nebula.grpc.ChunkRequest;
import com.nebula.grpc.PeerServiceGrpc;
import com.nebula.peer.service.FileService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class PeerMain {
    public static void main(String[] args) {
        SpringApplication.run(PeerMain.class, args);
    }

    @Bean
    CommandLineRunner run(FileService fileService) {
        return args -> {
            String testFilePath = "D:/seriess/The.Family.Man.S03E01.The.Peace.Problem.1080p.AMZN.WEB-DL.Hindi.DDP5.1.H.265-DUDU.mkv";

            System.out.println("--- 1. Splitting file ---");
            String hash = fileService.splitAndStore(testFilePath);
            System.out.println("File ready. Hash: " + hash);

            Thread.sleep(1000);

            System.out.println("--- 2. Connecting to myself ---");
            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                    .usePlaintext()
                    .build();

            PeerServiceGrpc.PeerServiceStub stub = PeerServiceGrpc.newStub(channel);

            // request the chunks back
            CountDownLatch finishLatch = new CountDownLatch(1);

            StreamObserver<ChunkRequest> requestObserver = stub.downloadChunk(new StreamObserver<ChunkData>() {
                @Override
                public void onNext(ChunkData chunkData) {
                    System.out.println("CLIENT: Received chunk data: " + chunkData.getChunkIndex() + " (" + chunkData.getData().size() + " bytes)");
                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println("Client: Error: " + throwable.getMessage());
                    finishLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    System.out.println("Client: Download complete");
                    finishLatch.countDown();
                }
            });

            // trying for first few chunks
            for (int i = 0; i < 3; i++) {
                requestObserver.onNext(ChunkRequest.newBuilder().setContentHash(hash).setChunkIndex(i).build());
                Thread.sleep(100);
            }

            requestObserver.onCompleted();

            finishLatch.await(5, TimeUnit.SECONDS);
            channel.shutdown();
        };
    }
}
