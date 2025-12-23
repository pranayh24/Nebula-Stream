package com.nebula.peer;

import com.nebula.grpc.*;
import com.nebula.peer.service.FileService;
import com.nebula.peer.service.TrackerClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class PeerMain {
    public static void main(String[] args) {
        SpringApplication.run(PeerMain.class, args);
    }

    @Bean
    CommandLineRunner run(FileService fileService, TrackerClient trackerClient) {
        return args -> {
            // 1. register
            System.out.println("--- 1. Registering with Tracker ---");
            boolean registered = trackerClient.register(9090);
            if (!registered) {
                System.err.println("Could not reach tracker");
                return;
            }

            // 2. split and announce
            System.out.println("--- 2. Creating content ---");
            String hash = fileService.splitAndStore("D:/seriess/The.Family.Man.S03E01.The.Peace.Problem.1080p.AMZN.WEB-DL.Hindi.DDP5.1.H.265-DUDU.mkv");
            System.out.println("File ready. Hash: " + hash);

            trackerClient.announce(hash);

            // 3. discovery test
            System.out.println("--- 3. Asking tracking for peers ---");
            Thread.sleep(5000);

            var peers = trackerClient.locate(hash);
            for (var peer : peers) {
                System.out.println(" - " + peer.getPeerId() + " at " + peer.getAddress() + ":" + peer.getPort());
            }

            System.out.println("Test Complete.");
        };
    }
}
