package com.ocp.aether.service;

import com.ocp.aether.config.SecurityService;
import com.ocp.aether.dto.MonitorRequest;
import com.ocp.aether.master.MasterRegistry;
import com.ocp.aether.model.Monitor;
import com.ocp.aether.repository.MonitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitorService {

    private final SecurityService securityService;
    private final MonitorRepository monitorRepository;
    private final MasterRegistry masterRegistry; // <--- Add this
    private final MasterNeuralService neuralService;

    public Monitor addUrl(MonitorRequest request) throws Exception {
        Monitor monitor = new Monitor();
        monitor.setSiteName(request.getSiteName());
        monitor.setUrl(request.getUrl());
        monitor.setRegion(request.getRegion()); // <--- Ensure region is set!

        String encrypted = securityService.encrypt(request.getEncryptedToken());
        monitor.setEncryptedToken(encrypted);
        Monitor saved = monitorRepository.save(monitor);

        // 2. Update the "Working Memory" (RAM)
        masterRegistry.hydrate(List.of(saved));

        // 3. THE DELTA PUSH: Tell the Agent about the new mission immediately
        neuralService.pushNewMissionToRegion(saved);

        return saved;
    }

    public List<Monitor> getAllTargets() {
        return monitorRepository.findAll();
    }
}
