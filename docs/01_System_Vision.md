# Introduction & Problem Statement

* **General Information:** Project Aether is an industrial-grade Master Node monitoring engine utilizing a dual-protocol design (REST for user CRUD, gRPC for internal node telemetry).
* **Problem Statement:** Legacy architectures suffer from OS thread exhaustion during massive network I/O and database degradation from unstructured time-series log ingestion.
* **Purpose:** To build a deterministic, non-blocking monitor bridging low-level hardware efficiency with high-level Java 21 architecture.
* **Project Objectives:** * Achieve 1M+ concurrent pings using Project Loom (Virtual Threads).
    * Maintain $\mathcal{O}(\log n)$ query efficiency using TimescaleDB temporal partitioning.
    * Enforce AES/GCM 256-bit encryption for at-rest token security.
* **Project Scope:** Encompasses the Pulse logic, Service/Model layers (`Monitor`, `PingLog`), database bridging (JPA), and full system containerization.