# Chapter 3: Data Architecture & Persistence Strategy

## 3.1 The Persistent Vault Design
Project Aether utilizes a dual-purpose data strategy designed to handle both **Relational Metadata** and **High-Frequency Time-Series Data**. The architecture is anchored by a PostgreSQL instance extended with the TimescaleDB engine to ensure the system remains performant as the User dashboard scales.

### 3.1.1 Relational Metadata (The 'monitors' Table)
The `monitors` table serves as the primary registry for all tracked assets.
*   **Primary Key:** UUID (Universally Unique Identifier).
*   **Indexing:** B-Tree.
*   **Logic:** By using a B-Tree index, the system achieves $O(\log n)$ lookup speeds. This allows the Master Node to find a specific monitoring target in approximately 20 steps, even within a pool of 1,000,000 registered URLs.

### 3.1.2 Time-Series Engine (The 'ping_logs' Table)
The historical latency data is stored in the `ping_logs` table, which is converted into a **TimescaleDB Hypertable**.



---

## 3.2 Partitioning & Hypertable Mechanics
To prevent performance degradation (disk-thrashing) as the log count reaches billions, we implement **Automatic Time-Partitioning**.

### 3.2.1 The "Time-Chunk" Strategy
Unlike standard PostgreSQL tables which grow into one massive file, a Hypertable slices data into "Chunks" based on time.
*   **Active Chunks:** Recent pings are stored in "Hot" chunks kept in RAM.
*   **Historical Chunks:** Older pings are moved to "Cold" storage, preventing the B-Tree index from becoming too large to fit in memory.

---

## 3.3 Composite Key Rationale
In the `ping_logs` table, we utilize a **Composite Primary Key** consisting of `(id, timestamp)`.

### 3.3.1 Data Locality
In computer architecture, data locality refers to the physical proximity of related bits on the storage medium.
*   By including the `timestamp` in the primary key, we force the database to physically cluster data by time.
*   **Impact:** When the system queries "latency for the last 24 hours," the disk head (or SSD controller) reads a contiguous block of data rather than jumping to random memory addresses. This is a direct application of the hardware principles discussed in Charles Petzold’s *Code*.

---

## 3.4 Data Integrity & Validation
To ensure the "Garbage In, Garbage Out" principle is mitigated, the data layer is protected by two gates:

1.  **Jakarta Validation Gate:** Prevents non-URL strings from entering the persistence layer.
2.  **Transactional Atomicity:** The `@Transactional` boundary in the `PulseService` ensures that a status update and a log entry are treated as a single "Atomic" unit of work.

### 3.4.1 Encryption at Rest
Sensitive tokens are never stored in plain text. Before the `INSERT` operation, the `SecurityService` applies **AES/GCM 256-bit encryption**, transforming the bits into a secure state that is unreadable without the master system key.



---

## 3.5 Scalability Analysis
As the system moves toward its goal of global monitoring, the database architecture supports:
*   **Parallel Ingestion:** Multiple "Pulse" threads can write to the same hypertable chunk simultaneously.
*   **Analytical Efficiency:** SQL functions like `time_bucket()` allow the Aether dashboard to aggregate millions of pings into 5-minute averages in milliseconds.