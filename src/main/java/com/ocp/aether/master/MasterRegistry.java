package com.ocp.aether.master;

import com.ocp.aether.model.Monitor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class MasterRegistry {
    private final Map<UUID, Monitor> monitorMap = new ConcurrentHashMap<>();

    public void hydrate(List<Monitor> monitors) {
        monitors.forEach(m -> monitorMap.put(UUID.fromString(m.getId()), m));
    }

    public List<Monitor> getMissionsByRegion(String region) {
        return monitorMap.values().stream()
                .filter(m -> m.getRegion().equalsIgnoreCase(region))
                .collect(Collectors.toList());
    }
    public Monitor getMonitorById(UUID id) {
        return monitorMap.get(id);
    }
}
