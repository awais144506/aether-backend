# 🌌 Project Aether: Global Monitoring Mastermind

**Project Aether** is a high-performance, scalable URL monitoring system designed to handle massive amounts of real-time latency data. Built with a focus on **concurrency**, **security**, and **low-level optimization**, it serves as the persistent backbone for the apps monitoring.

---

## 🚀 Key Features

*   **Massive Concurrency:** Utilizes **Java Virtual Threads** (Project Loom) to execute thousands of pings in parallel without blocking the OS kernel.
*   **Time-Series Scaling:** Powered by **TimescaleDB (PostgreSQL)**. Uses **Hypertables** to partition logs by time, ensuring constant $O(1)$ insert speeds even with millions of records.
*   **Industrial Security:** Implements a "Security Vault" using **AES/GCM 256-bit encryption** to protect sensitive monitor tokens before they touch the persistent storage.
*   **Validated Ingestion:** Strict input validation gates (Jakarta Validation) ensure data integrity and prevent "Garbage In, Garbage Out."
*   **Self-Healing Pulse:** A transactional `@Scheduled` heartbeat that pings targets every 60 seconds and atomically updates statuses and logs.

---

## 🛠️ The Tech Stack

*   **Language:** Java 21+
*   **Framework:** Spring Boot 4.x (Spring Data JPA, Web, Validation, Actuator)
*   **Database:** PostgreSQL + TimescaleDB (Running in Docker)
*   **Concurrency:** Structured Concurrency via `VirtualThreadPerTaskExecutor`
*   **Security:** AES/GCM 256-bit Encryption
*   **Frontend:** Next.js (Planned/Integrated)

---

## 🏗️ Architecture & CS Principles

### 1. Data Locality & Search Efficiency
The system uses **B-Tree Indexing** for metadata lookups, reducing search time from linear $O(n)$ to logarithmic $O(\log n)$. For time-series logs, we use **Composite Primary Keys** `(UUID + Timestamp)` to ensure data is physically stored in chunks that align with time-range queries.

### 2. The Clock Cycle
The system operates on a deterministic clock pulse using `@Scheduled(fixedDelay = 30000)`. This mimics the hardware clock cycles discussed in Charles Petzold's *Code*, providing a reliable interval for the "State Machine" to refresh the status of all monitored nodes.

---

## 🚦 Getting Started

### 1. Database (Docker)
The system requires a **TimescaleDB** instance. Run the following to start the vault:
```bash
docker run -d --name aether -p 5438:5432 \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=pak123 \
  timescale/timescaledb:latest-pg18
```

### 2. Initialize Hypertable
Connect to the database and run the scaling command to optimize the log table:

SQL
``` bash
SELECT create_hypertable('ping_logs', 'timestamp');
```
### 3. Application Setup
Configure your application.properties:
Properties
```bash
spring.datasource.url=jdbc:postgresql://localhost:5438/postgres
spring.datasource.username=user
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```
### 📈 Roadmap (The Modular Future)
[ ] Regional Agents: Deploy Spring Boot agents in London and New York to check latency from different global points.

[ ] Real-time Dashboard: Connect the Next.js frontend to visualize latency fluctuations.

[ ] Alerting Engine: Integration with Email/Slack for instant "Down" notifications.