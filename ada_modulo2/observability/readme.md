# Local Tracing Setup

This project now ships with a local tracing stack for Docker Compose:

- `order-service` exports traces via OTLP
- `otel-collector` receives and forwards them
- `tempo` stores and serves traces
- `grafana` exposes a UI for metrics and traces

## What it does

- Receives OTLP traces on `4318`
- Stores traces in Tempo
- Keeps the collector `debug` exporter enabled for local verification in logs
- Avoids the `localhost:4318` connection-refused error inside the app container

## Run it

```powershell
cd C:\Users\fsanc\IdeaProjects\order-service-ada-m2
docker compose up -d --build order-service otel-collector tempo grafana prometheus
```

## Verify with logs

```powershell
Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8080/v3/api-docs'
docker compose logs otel-collector --tail 120
```

## Verify with Grafana

1. Open `http://localhost:3000`
2. Login with `admin / admin` unless overridden by env vars
3. Open **Explore**
4. Select the `Tempo` datasource
5. Search for recent traces

The `Prometheus` and `Tempo` datasources are provisioned automatically.

