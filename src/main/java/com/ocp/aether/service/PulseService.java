package com.ocp.aether.service;

import com.ocp.aether.master.MasterRegistry;
import com.ocp.aether.model.Monitor;
import com.ocp.aether.model.PingLog;
import com.ocp.aether.model.PingLogId;
import com.ocp.aether.repository.MonitorRepository;
import com.ocp.aether.repository.PingLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PulseService {

    private final MonitorRepository monitorRepository;
    private final PingLogRepository pingLogRepository;
    private final MasterRegistry masterRegistry;
    @Transactional
    public void recordPulseFromAgent(String monitorId, int statusCode, long latency, String region) {
        // 1. Find the Monitor in our "Working Memory" (RAM)
        Monitor monitor = masterRegistry.getMonitorById(UUID.fromString(monitorId));

        if (monitor != null) {
            // 2. Update the Live Status
            String statusString = (statusCode >= 200 && statusCode < 300) ? "UP ✅" : "DOWN ❌";
            monitor.setStatus(statusString);

            // 3. Update the Database Status (Persistence)
            monitorRepository.save(monitor);

            // 4. Create the Time-Series Log (The History)
            PingLog log = new PingLog();
            PingLogId logId = new PingLogId(UUID.randomUUID(), OffsetDateTime.now());
            log.setId(logId);
            log.setMonitor(monitor);
            log.setStatusCode(statusCode);
            log.setResponseTimeMs(latency);

            pingLogRepository.save(log);

            System.out.printf("[%s] Logged Pulse: %s | Status: %d | Latency: %dms%n",
                    region, monitor.getUrl(), statusCode, latency);
        }
    }
}