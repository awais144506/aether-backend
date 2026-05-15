package com.ocp.aether.agent;

import com.ocp.aether.utility.HttpClientUtility;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AgentPulseExecutor {
    private final String region = System.getenv().getOrDefault("AGENT_REGION", "DEVELOPMENT-LOCAL");
    public void executePulse(String url, String monitorId) {
        Thread.startVirtualThread(() -> {
            long startTime = System.currentTimeMillis();
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                HttpResponse<Void> response = HttpClientUtility.httpClient.send(request, HttpResponse.BodyHandlers.discarding());
                long latency = System.currentTimeMillis() - startTime;

                System.out.printf("Region Observation: %s | Status: %d | Latency: %dms%n",
                        url, response.statusCode(), latency);
            } catch (Exception e) {
                System.err.println("Pulse Failed for " + url + ": " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
