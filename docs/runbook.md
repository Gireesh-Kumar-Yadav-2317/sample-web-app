# Runbook

A condensed operations cheat-sheet for the deployed service.

## Useful endpoints

| Endpoint | Use |
| --- | --- |
| `GET /actuator/health` | Combined health |
| `GET /actuator/health/liveness` | "Is the JVM alive?" — used by `livenessProbe` |
| `GET /actuator/health/readiness` | "Can the app serve traffic?" — used by `readinessProbe` |
| `GET /actuator/prometheus` | Scrape target for Prometheus |
| `GET /actuator/metrics/{name}` | Drill into individual metrics |
| `GET /actuator/info` | Build info |

## Common runbook entries

### The pod is in CrashLoopBackOff

1. `kubectl -n sample-web-app logs deploy/sample-web-app --previous` — look for the last fatal log line.
2. Check `kubectl -n sample-web-app describe pod <name>` for an OOMKilled event. If yes, raise `resources.limits.memory` or lower `MaxRAMPercentage`.
3. If logs show Flyway errors, check whether a previous migration was applied partially. `kubectl -n sample-web-app exec -it deploy/postgres -- psql -U sample -c 'select * from flyway_schema_history order by installed_rank desc limit 5;'`

### Readiness flapping

- The default readiness probe checks `/actuator/health/readiness`. If you're seeing it flap, look at the `db` indicator — it usually means the DB is rejecting connections or hit max-pool.
- Increase `failureThreshold` only as a last resort; usually you want to fix the DB symptom.

### High latency / CPU

- The HPA scales on `averageUtilization: 70%` CPU and `80%` memory. If you're saturated and not scaling, check whether the HPA hit `maxReplicas`.
- Hot endpoints can be inspected via `GET /actuator/metrics/http.server.requests` with `tag=uri:...`.

### Rolling deploy stuck

- The rolling strategy is `maxUnavailable: 0, maxSurge: 1` — a stuck rollout usually means the new pod fails readiness. `kubectl rollout status` + `kubectl describe pod` on the new pod.

## SLO notes

This is a reference app, not a real one. Suggested SLOs if you're using it as a teaching template:

- Availability: 99.9% measured by `/actuator/health/readiness` over rolling 30 days
- Latency: p95 < 200 ms for `GET /api/items` on a warm JVM
- Error budget: 43 minutes / 30 days
