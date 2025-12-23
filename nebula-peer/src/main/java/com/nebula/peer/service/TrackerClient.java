package com.nebula.peer.service;

import com.nebula.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@Component
public class TrackerClient {

    private TrackerServiceGrpc.TrackerServiceBlockingStub blockingStub;
    private final String peerId;

    public TrackerClient() {
        this.peerId = UUID.randomUUID().toString();
    }

    @PostConstruct
    public void init() {
        // connect to the tracker at localhost:9090
        ManagedChannel channel = ManagedChannelBuilder.forAddress( "localhost", 9000)
                .usePlaintext()
                .build();

        this.blockingStub = TrackerServiceGrpc.newBlockingStub(channel);
    }

    public boolean register(int myPort) {
        System.out.println("Connecting to Tracker...");
        try {
            PeerRegistration request = PeerRegistration.newBuilder()
                    .setPeerId(peerId)
                    .setIpAddress("localhost") // InetAddress.getLocalHost()
                    .setPort(myPort)
                    .build();

            RegistrationResponse response = blockingStub.registerPeer(request);
            System.out.println("Tracker Response: " + response.getMessage());
            return response.getSuccess();
        } catch (Exception e) {
            System.err.println("Failed to connect to Tracker: " + e.getMessage());
            return false;
        }
    }

    public void announce(String contentHash) {
        try {
            ChunkAnnouncement request = ChunkAnnouncement.newBuilder()
                    .setPeerId(peerId)
                    .setContentHash(contentHash)
                    .build();

            blockingStub.announceChunk(request);
            System.out.println("Announced to Tracker: I have " + contentHash);
        } catch (Exception e) {
            System.err.println("Announce failed: " + e.getMessage());
        }
    }

    public List<PeerInfo> locate(String contentHash) {
        ContentRequest request = ContentRequest.newBuilder()
                .setContentHash(contentHash)
                .build();

        PeerList list = blockingStub.locateContent(request);
        return list.getPeersList();
    }
}
