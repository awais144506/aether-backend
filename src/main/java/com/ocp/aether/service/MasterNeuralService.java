package com.ocp.aether.service;

import com.ocp.aether.grpc.AetherNeuralLinkGrpc;
import com.ocp.aether.grpc.MissionPacket;
import com.ocp.aether.grpc.PulseResult;
import com.ocp.aether.master.MasterRegistry;
import com.ocp.aether.model.Monitor;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
@RequiredArgsConstructor
public class MasterNeuralService extends AetherNeuralLinkGrpc.AetherNeuralLinkImplBase {

    private final MasterRegistry registry;
    private final PulseService pulseService;
    private final Map<String, StreamObserver<MissionPacket>> activeAgentStreams = new ConcurrentHashMap<>();

    @Override
    public StreamObserver<PulseResult> streamNeuralLink(StreamObserver<MissionPacket> responseObserver) {
        return new StreamObserver<PulseResult>() {
            @Override
            public void onNext(PulseResult result) {
                System.out.println("MASTER: Received signal from region [" + result.getRegion() + "] with ID: " + result.getMonitorId());

                // Register the stream
                activeAgentStreams.put(result.getRegion(), responseObserver);

                if ("HANDSHAKE".equals(result.getMonitorId())) {
                    System.out.println("MASTER: Handshake confirmed for " + result.getRegion() + ". Dispatching missions...");
                    pushMissionToAgent(responseObserver, result.getRegion());
                } else {
                    pulseService.recordPulseFromAgent(
                            result.getMonitorId(),
                            result.getStatusCode(),
                            result.getLatencyMs(),
                            result.getRegion()
                    );
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("MASTER: Neural Link Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("MASTER: Neural Link Completed by Agent.");
            }
        };
    }

    public void pushMissionToAgent(StreamObserver<MissionPacket> agentStream, String region) {
        var missions = registry.getMissionsByRegion(region);
        System.out.println("MASTER: Found " + missions.size() + " missions for region: " + region);

        if (missions.isEmpty()) {
            System.out.println("⚠️ WARNING: No monitors found in DB for region '" + region + "'. Agent will have nothing to do!");
        }

        missions.forEach(monitor -> {
            MissionPacket packet = MissionPacket.newBuilder()
                    .setMonitorId(monitor.getId())
                    .setUrl(monitor.getUrl())
                    .setTimeoutMs(5000)
                    .build();
            agentStream.onNext(packet);
            System.out.println("MASTER: Pushed mission [" + monitor.getUrl() + "] to " + region);
        });
    }

    public void pushNewMissionToRegion(Monitor monitor) {
        StreamObserver<MissionPacket> stream = activeAgentStreams.get(monitor.getRegion());
        if (stream != null) {
            MissionPacket packet = MissionPacket.newBuilder()
                    .setMonitorId(monitor.getId())
                    .setUrl(monitor.getUrl())
                    .setTimeoutMs(5000)
                    .build();
            stream.onNext(packet);
            System.out.println("MASTER: Pushed DELTA update for " + monitor.getUrl());
        }
    }
}