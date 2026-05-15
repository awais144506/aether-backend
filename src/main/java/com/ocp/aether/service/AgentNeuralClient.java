package com.ocp.aether.service;

import com.ocp.aether.grpc.AetherNeuralLinkGrpc;
import com.ocp.aether.grpc.MissionPacket;
import com.ocp.aether.grpc.PulseResult;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AgentNeuralClient {
    private final Map<String, MissionPacket> localMissions = new ConcurrentHashMap<>();
    private final AtomicReference<StreamObserver<PulseResult>> feedbackStream = new AtomicReference<>();

    private final String myRegion = System.getenv().getOrDefault("AGENT_REGION", "US-EAST-1");
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public void startNervousSystem(String masterHost, int port) {
        System.out.println("AGENT: Opening channel to " + masterHost + ":" + port);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(masterHost, port)
                .usePlaintext()
                .build();

        AetherNeuralLinkGrpc.AetherNeuralLinkStub asyncStub = AetherNeuralLinkGrpc.newStub(channel);

        StreamObserver<PulseResult> requestObserver = asyncStub.streamNeuralLink(new StreamObserver<MissionPacket>() {
            @Override
            public void onNext(MissionPacket mission) {
                System.out.println("AGENT: Received Mission from Master: " + mission.getUrl());
                localMissions.put(mission.getMonitorId(), mission);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("AGENT: Link Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("AGENT: Link Closed by Master.");
            }
        });

        feedbackStream.set(requestObserver);

        // SEND HANDSHAKE
        System.out.println("AGENT: Sending Handshake for region: " + myRegion);
        feedbackStream.get().onNext(PulseResult.newBuilder()
                .setRegion(myRegion)
                .setMonitorId("HANDSHAKE")
                .build());
    }

    @Scheduled(fixedRate = 10000) // Reduced to 10s for faster testing
    public void runLocalLoop() {
        if (feedbackStream.get() == null) {
            System.out.println("AGENT LOOP: Waiting for gRPC connection...");
            return;
        }
        if (localMissions.isEmpty()) {
            System.out.println("AGENT LOOP: Connected, but no missions received yet. Check Master logs for Region match.");
            return;
        }

        System.out.println("AGENT LOOP: Executing " + localMissions.size() + " pings...");
        for (MissionPacket mission : localMissions.values()) {
            performObservation(mission, feedbackStream.get());
        }
    }

    public void performObservation(MissionPacket mission, StreamObserver<PulseResult> feedback) {
        Thread.startVirtualThread(() -> {
            long startTime = System.currentTimeMillis();
            int code = 0;
            try {
                var request = HttpRequest.newBuilder().uri(URI.create(mission.getUrl())).GET().build();
                var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
                code = response.statusCode();
                System.out.println("AGENT: Ping Success [" + code + "] for " + mission.getUrl());
            } catch (Exception e) {
                System.err.println("AGENT: Ping Failed for " + mission.getUrl());
            } finally {
                feedback.onNext(PulseResult.newBuilder()
                        .setMonitorId(mission.getMonitorId())
                        .setStatusCode(code)
                        .setLatencyMs(System.currentTimeMillis() - startTime)
                        .setRegion(myRegion)
                        .build());
            }
        });
    }
}