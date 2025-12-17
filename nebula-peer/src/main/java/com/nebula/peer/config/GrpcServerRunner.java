package com.nebula.peer.config;

import com.nebula.peer.service.PeerServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class GrpcServerRunner implements CommandLineRunner {

    private final PeerServiceImpl peerService;
    private Server server;

    public GrpcServerRunner(PeerServiceImpl peerService) {
        this.peerService = peerService;
    }

    @Override
    public void run(String... args) throws Exception {
        int port = 9090;
        server = ServerBuilder.forPort(port)
                .addService(peerService)
                .build()
                .start();

        System.out.println("--- gRPC Peer Node Started on Port " + port + "---");

    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
            System.out.println("--- gRPC Peer Node Stopped ---");
        }
    }
}
