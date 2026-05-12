# Architecture

A short tour through the layers of this reference app — useful both as documentation and as a script for tutorial videos.

## Layers

```
HTTP request
    │
    ▼
ItemController          (REST adapter — DTOs, validation, error mapping)
    │
    ▼
ItemService             (transaction boundary, business rules, timestamps via Clock)
    │
    ▼
ItemRepository          (Spring Data JPA)
    │
    ▼
PostgreSQL / H2         (schema managed by Flyway)
```

## Why this shape

- **`Item` is rich-ish, not anaemic.** It carries its own lifecycle timestamps via `onCreate` / `onUpdate`, fed a `Clock` from the service. Tests stay deterministic without time mocking magic.
- **DTOs live with the controller** as nested `record`s. Tutorial-friendly (one file to look at), good enough for a single-purpose service.
- **Bean Validation at the edge.** `@Valid` on the request body, `@NotBlank` / `@Min` on the record. Domain doesn't have to defend against null again.
- **Flyway, not `ddl-auto: update`.** Production-style. The migration is in `src/main/resources/db/migration/`.

## Observability

- `/actuator/health` exposes Kubernetes-friendly `/health/liveness` and `/health/readiness` probes.
- `/actuator/prometheus` is wired through Micrometer.
- Logs are emitted as one-line JSON via the pattern in `application.yml` — drop straight into Loki / CloudWatch Logs.

## Container

- Multi-stage Dockerfile.
- Build stage uses `maven:3.9-eclipse-temurin-21`.
- Runtime stage uses `eclipse-temurin:21-jre-jammy`, runs as a non-root user, with `JAVA_TOOL_OPTIONS` set to use G1GC and respect the container's memory.
- Spring Boot's `layertools` mode is used so dependency layers cache independently of application code.

## Kubernetes

- `Deployment` with anti-affinity-via-topology-spread, startup/liveness/readiness probes, resource requests + limits, `runAsNonRoot`, `readOnlyRootFilesystem`, `seccompProfile: RuntimeDefault`, dropped capabilities, and `automountServiceAccountToken: false`.
- `Service` is `ClusterIP`.
- `Ingress` annotated for nginx by default (swap the class for ALB / Traefik etc.).
- `HorizontalPodAutoscaler` v2 scales on CPU + memory.
- `PodDisruptionBudget` keeps at least one pod up during voluntary disruptions.
