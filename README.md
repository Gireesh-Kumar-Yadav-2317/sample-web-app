# sample-web-app

A small, production-shaped Spring Boot 3 reference web app — the "thing to deploy" that pairs with [`aws-infrastructure-as-code`](https://github.com/onemoretechie/aws-infrastructure-as-code) and the rest of the OneMoreTechie tutorial series.

It is deliberately simple but **complete**:

- Java 21, Spring Boot 3.3.x, Maven
- REST API (`/api/items`) backed by JPA + Flyway migrations
- H2 for tests, PostgreSQL for runtime
- Actuator + Prometheus metrics, structured JSON logging, graceful shutdown
- Multi-stage Dockerfile producing a small, non-root image
- `docker-compose.yml` for local dev (app + Postgres)
- GitHub Actions CI: tests, dependency caching, image build, GHCR push
- Kubernetes manifests under [`deploy/k8s/`](deploy/k8s/) and a Helm chart under [`deploy/helm/sample-web-app/`](deploy/helm/sample-web-app/)

## Quick start

### Prerequisites

- Java 21 (Temurin recommended)
- Maven 3.9+
- Docker (for the compose / K8s flows)

### Run locally (no Docker)

```bash
mvn spring-boot:run
curl http://localhost:8080/actuator/health
```

The default profile uses H2 in-memory, so no extra setup is needed.

### Run with Docker Compose (app + Postgres)

```bash
docker compose up --build
curl http://localhost:8080/api/items
```

### Run on Kubernetes

```bash
kubectl apply -f deploy/k8s/
kubectl -n sample-web-app port-forward svc/sample-web-app 8080:80
```

…or via Helm:

```bash
helm install swa deploy/helm/sample-web-app \
  --namespace sample-web-app --create-namespace
```

## API

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/items` | List all items |
| `GET` | `/api/items/{id}` | Get one item |
| `POST` | `/api/items` | Create an item (JSON body: `{"name":"...","quantity":1}`) |
| `PUT` | `/api/items/{id}` | Update |
| `DELETE` | `/api/items/{id}` | Delete |
| `GET` | `/actuator/health` | Liveness + readiness combined |
| `GET` | `/actuator/prometheus` | Prometheus scrape endpoint |

## Project layout

```
src/main/java/com/onemoretechie/sample/
  SampleWebAppApplication.java
  config/ObservabilityConfig.java
  controller/{HealthController,ItemController}.java
  domain/Item.java
  repository/ItemRepository.java
  service/ItemService.java
src/main/resources/
  application.yml                # base profile (H2)
  application-docker.yml         # Docker Compose / Postgres profile
  db/migration/V1__create_items_table.sql
deploy/
  k8s/      # raw Kubernetes manifests
  helm/sample-web-app/   # Helm chart
.github/workflows/ci.yml
Dockerfile
docker-compose.yml
```

## Tutorial roadmap

| # | Topic |
| - | --- |
| 01 | Bootstrapping a Spring Boot 3 service from `start.spring.io` (manually) |
| 02 | A real REST resource: layered controller / service / repository |
| 03 | Flyway migrations and per-profile data sources |
| 04 | Actuator, Micrometer, Prometheus, structured logs |
| 05 | A small, secure multi-stage Dockerfile |
| 06 | GitHub Actions CI: cache, test, build, push to GHCR |
| 07 | Deploying to Kubernetes — raw manifests |
| 08 | Same deployment as a Helm chart, with multi-env values |
| 09 | HPA, PDB, and probing for production readiness |

## License

[MIT](LICENSE).
