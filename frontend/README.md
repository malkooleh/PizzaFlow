# PizzaFlow Frontend

React + TypeScript single-page app for PizzaFlow.

## What this service does

- Serves role-based UI for `CUSTOMER`, `KITCHEN_STAFF`, `COURIER`, `RESTAURANT_MANAGER`, and `SYSTEM_ADMIN`
- Connects to backend APIs through the API Gateway (`/api`)
- Uses Keycloak OIDC for authentication
- Uses WebSocket/STOMP for kitchen real-time updates

## Tech stack

- React 19 + TypeScript
- Vite 6
- TanStack Router + TanStack Query
- Zustand
- Tailwind CSS v4
- Vitest + Playwright

## Prerequisites

- Node.js 22+
- npm 10+
- Backend stack running (at minimum API Gateway + Keycloak)

Windows note:

- In PowerShell on this machine, use `npm.cmd` instead of `npm` to avoid execution-policy issues from `npm.ps1`.

## Environment variables

The app reads variables from `frontend/.env` and `frontend/.env.development`.

| Variable | Required | Default | Description |
|---|---|---|---|
| `VITE_API_URL` | Yes | `http://localhost:8080` in `.env`, empty in `.env.development` | API base URL. Empty in dev means Vite proxy handles `/api` calls. |
| `VITE_KEYCLOAK_URL` | Yes | `http://localhost:9090` | Keycloak base URL |
| `VITE_KEYCLOAK_REALM` | Yes | `pizzaflow` | Keycloak realm |
| `VITE_KEYCLOAK_CLIENT_ID` | Yes | `pizzaflow-web` | OIDC client ID |
| `VITE_KITCHEN_WS_URL` | Yes | `ws://localhost:8084/ws/kitchen` | Kitchen WebSocket endpoint |
| `VITE_MAPBOX_TOKEN` | Optional | placeholder token in repo | Mapbox token for delivery maps |

## Install

From repository root:

```bash
cd frontend
npm.cmd ci
```

## Run locally (development)

```bash
cd frontend
npm.cmd run dev
```

- App URL: `http://localhost:4200`
- Vite proxy forwards:
- `/api` -> `http://localhost:8080`
- `/ws` -> `ws://localhost:8084`

## Build

```bash
cd frontend
npm.cmd run build
```

This does:

1. `tsr generate` (TanStack route tree generation)
2. `tsc -b` (type build)
3. `vite build`

Output is generated in `frontend/dist/`.

## Preview production build

```bash
cd frontend
npm.cmd run preview
```

## Quality and tests

Lint:

```bash
cd frontend
npm.cmd run lint
```

Unit/component tests (Vitest):

```bash
cd frontend
npm.cmd run test
```

Coverage:

```bash
cd frontend
npm.cmd run test:coverage
```

E2E (Playwright):

```bash
cd frontend
npm.cmd run test:e2e
```

Notes:

- `test:e2e` auto-installs Playwright browsers before running tests.
- Playwright tests are in `frontend/e2e/`.

## Docker

Build image from repository root (Dockerfile expects `frontend/...` paths in context):

```bash
docker build -f frontend/Dockerfile -t pizzaflow/frontend:local .
```

Run container:

```bash
docker run --rm -p 4200:80 pizzaflow/frontend:local
```

The container serves static files with Nginx.

## Troubleshooting

App starts but API calls fail:

- Ensure API Gateway is running on `8080`
- Check `VITE_API_URL` and Vite proxy behavior

Login loop or auth errors:

- Verify Keycloak URL/realm/client ID variables
- Ensure Keycloak client redirect URI includes `http://localhost:4200/callback`

Kitchen board real-time updates missing:

- Verify `VITE_KITCHEN_WS_URL`
- Ensure kitchen service WebSocket endpoint is reachable

Maps not rendering:

- Provide a valid `VITE_MAPBOX_TOKEN`

## Related files

- `frontend/package.json` - scripts and dependencies
- `frontend/vite.config.ts` - dev server/proxy/build config
- `frontend/playwright.config.ts` - E2E config
- `frontend/Dockerfile` - container build
- `frontend/nginx.conf` - runtime Nginx site config
