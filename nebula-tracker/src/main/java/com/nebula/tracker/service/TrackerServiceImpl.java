package com.nebula.tracker.service;

import com.nebula.grpc.*;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrackerServiceImpl extends TrackerServiceGrpc.TrackerServiceImplBase {
    // peerId -> peerInfo
    private final Map<String, PeerInfo> onlinePeers = new ConcurrentHashMap<>();

    // contentHash -> List of peerIds
    private final Map<String, Set<String>> contentIndex = new ConcurrentHashMap<>();

    public void registerPeer(PeerRegistration request, StreamObserver<RegistrationResponse> responseObserver) {
        String peerId = request.getPeerId();

        PeerInfo info = PeerInfo.newBuilder()
                .setPeerId(peerId)
                .setAddress(request.getIpAddress())
                .setPort(request.getPort())
                .setReputation(1.0f)
                .build();

        onlinePeers.put(peerId, info);
        System.out.println("TRACKER: Peer Registered -> " + peerId + " at " + request.getIpAddress());

        RegistrationResponse response = RegistrationResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Welcome to NebulaStream!")
                .setRefreshIntervalSeconds(30)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void announceChunk(ChunkAnnouncement request, StreamObserver<Ack> responseObserver) {
        // for now, we will track the whole file

        String contentHash = request.getContentHash();
        String peerId = request.getPeerId();

        contentIndex.computeIfAbsent(contentHash, k -> ConcurrentHashMap.newKeySet()).add(peerId);

        System.out.println("TRACKER: " + peerId + " has now content " + contentHash);

        responseObserver.onNext(Ack.newBuilder().setSuccess(true).setMessage("Updated").build());
        responseObserver.onCompleted();
    }

    @Override
    public void locateContent(ContentRequest request, StreamObserver<PeerList> responseObserver) {
        String hash = request.getContentHash();
        Set<String> holderIds = contentIndex.getOrDefault(hash, Collections.emptySet());

        PeerList.Builder responseBuilder =  PeerList.newBuilder();
        for (String peerId : holderIds) {
            PeerInfo info = onlinePeers.get(peerId);
            if (info != null) {
                responseBuilder.addPeers(info);
            }
        }

        System.out.println("TRACKER: " + hash + " -> Found " + responseBuilder.getPeersCount() + " peers");

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    public void keepAlive(PeerId request, StreamObserver<Ack> responseObserver) {
        // refresh timestamp (todo)
        responseObserver.onNext(Ack.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }
}
