package com.ocp.aether.utility;

import com.ocp.aether.service.AgentNeuralClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2) // Run after the Master's hydration
public class AgentTrigger implements CommandLineRunner {

    private final AgentNeuralClient agentClient;

    public AgentTrigger(AgentNeuralClient agentClient) {
        this.agentClient = agentClient;
    }

    @Override
    public void run(String... args) {
        System.out.println("Agent connecting to Neural Link...");
        agentClient.startNervousSystem("localhost", 9001);
    }
}