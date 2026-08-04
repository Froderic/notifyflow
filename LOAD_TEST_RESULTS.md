# NotifyFlow — Load Test Results

Load testing performed with [Gatling](https://gatling.io/) 3.15.1 against a locally running instance.

## Test Configuration

| Parameter | Value |
|---|---|
| Tool | Gatling 3.15.1 |
| Simulation | `EventPublishSimulation` |
| Duration | 39 seconds |
| Scenarios | ORDER_PLACED (ramp 50 users / sustain 20 req/s), PASSWORD_RESET (ramp 30 users / sustain 10 req/s) |
| Total virtual users | ~80 concurrent at peak |
| Endpoint | `POST /api/events/publish` |

## Results

| Metric | Value |
|---|---|
| Total requests | 980 |
| Throughput | 24.5 requests/second |
| Success rate | 100% (0 failures) |
| p50 latency | 4ms |
| p75 latency | 5ms |
| p95 latency | 6ms |
| p99 latency | 7ms |
| Max latency | 13ms |
| Std deviation | 1ms |

## Assertions (both passed)

- ✅ p99 response time < 2000ms
- ✅ Success rate > 95%

## Notes

Response times reflect **Kafka producer latency only** — the endpoint returns `202 Accepted` immediately after publishing to the topic, not after consumer processing completes. This is by design: the async pipeline decouples ingestion speed from processing speed. Consumer processing (email logging, webhook delivery, audit logging) happens independently and asynchronously after the producer returns.

The standard deviation of 1ms indicates highly consistent performance under load with no meaningful variance — a sign of a stable, well-behaved async pipeline.

## Running the Load Test

Ensure the app is running first:

```bash
docker compose up -d
./gradlew bootRun
```

Then in a separate terminal:

```bash
./gradlew gatlingRun
```

HTML report generated at `build/reports/gatling/<timestamp>/index.html`.