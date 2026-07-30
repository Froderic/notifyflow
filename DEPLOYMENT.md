# NotifyFlow — Deployment Architecture

This document describes the production deployment architecture for NotifyFlow on AWS,
and an alternative lightweight deployment on Railway for demo/portfolio purposes.

---

## AWS Architecture (Production Design)

### Region
All resources deployed in `ca-central-1` (Canada Central) for low latency
and Canadian data residency.

### Architecture Diagram

```text
┌─────────────────────────────────────────────────────────────┐
│                        VPC (ca-central-1)                   │
│                                                             │
│   ┌─────────────┐     ┌─────────────────────────────────┐  │
│   │   Public    │     │         Private Subnet          │  │
│   │   Subnet    │     │                                 │  │
│   │             │     │  ┌──────────┐  ┌─────────────┐ │  │
│   │ ┌─────────┐ │     │  │   RDS    │  │  ElastiCache│ │  │
│   │ │   EC2   │─┼─────┼─▶│Postgres │  │   (Redis)   │ │  │
│   │ │ (app)   │ │     │  └──────────┘  └─────────────┘ │  │
│   │ └────┬────┘ │     │                                 │  │
│   └──────┼──────┘     │  ┌──────────────────────────┐  │  │
│          │            │  │      Amazon MSK           │  │  │
│          └────────────┼─▶│  (Kafka, KRaft mode)      │  │  │
│                       │  │  3 brokers, ca-central-1  │  │  │
│                       │  └──────────────────────────┘  │  │
│                       └─────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
         │
    Internet Gateway
         │
    (HTTP/HTTPS traffic)
```

### Services

| Service | AWS Resource | Config |
|---|---|---|
| Spring Boot app | EC2 t3.small | Public subnet, port 8080 |
| PostgreSQL | RDS db.t3.micro | Private subnet, Multi-AZ off (demo) |
| Redis | ElastiCache cache.t3.micro | Private subnet |
| Kafka | Amazon MSK | Private subnet, 3 brokers, kafka.t3.small |

### Security Groups

**EC2 (app):**
- Inbound: port 8080 from 0.0.0.0/0 (HTTP)
- Outbound: port 5432 to RDS SG, port 6379 to ElastiCache SG, port 9092 to MSK SG

**RDS:**
- Inbound: port 5432 from EC2 SG only
- No public access

**ElastiCache:**
- Inbound: port 6379 from EC2 SG only
- No public access

**MSK:**
- Inbound: port 9092 from EC2 SG only
- No public access

### IAM
- EC2 instance role with MSK read/write permissions
- No hardcoded credentials — all connection strings via environment variables

### Environment Variables (EC2)

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://<rds-endpoint>:5432/notifyflow
SPRING_DATASOURCE_USERNAME=notifyflow
SPRING_DATASOURCE_PASSWORD=<secret>
SPRING_DATA_REDIS_HOST=<elasticache-endpoint>
SPRING_DATA_REDIS_PORT=6379
SPRING_KAFKA_BOOTSTRAP_SERVERS=<msk-broker-1>:9092,<msk-broker-2>:9092,<msk-broker-3>:9092
```

### MSK Configuration

- **Kafka version:** 3.9.0
- **Mode:** KRaft (no Zookeeper)
- **Brokers:** 3 (one per Availability Zone for fault tolerance)
- **Replication factor:** 3 (production) vs 1 (local dev)
- **Partitions:** 6 for `notification-events`, 3 for `notification-events-dlq`
- **Authentication:** IAM authentication via AWS MSK IAM

### Key Production Differences vs Local Dev

| Concern | Local Dev | AWS Production |
|---|---|---|
| Kafka | Single KRaft broker, Docker | MSK, 3 brokers across 3 AZs |
| Replication factor | 1 | 3 |
| DB | Docker Postgres, `create-drop` | RDS Postgres, Flyway migrations |
| Redis | Docker Redis | ElastiCache |
| Secrets | `application.yml` | Environment variables / AWS Secrets Manager |
| TLS | None | MSK TLS + RDS SSL |

---

## Railway Deployment (Demo/Portfolio)

For a lightweight demo deployment without AWS costs, NotifyFlow can be deployed
on [Railway](https://railway.app) using Docker Compose.

### Services on Railway

| Service | Railway Config |
|---|---|
| Spring Boot app | Dockerfile, port 8080 |
| PostgreSQL | Railway Postgres plugin |
| Redis | Railway Redis plugin |
| Kafka | Docker image: `apache/kafka:3.9.0`, KRaft mode |

### Environment Variables (Railway)

Railway injects `DATABASE_URL`, `REDIS_URL` automatically from plugins.
Map them in `application.yml`:

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
  data:
    redis:
      url: ${REDIS_URL}
  kafka:
    bootstrap-servers: ${KAFKA_BROKER_URL:localhost:9092}
```

### Limitations vs AWS

- Single Kafka broker (no replication, no fault tolerance)
- No multi-AZ redundancy
- Suitable for demo/portfolio purposes only

---

## Local Development

See [README.md](README.md) for local development setup using Docker Compose.

All infrastructure (Kafka, PostgreSQL, Redis) runs locally via Docker:

```bash
docker-compose up -d
./gradlew bootRun
```