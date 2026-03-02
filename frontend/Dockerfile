# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM node:22-alpine AS builder

WORKDIR /app

# Copy manifests first for layer caching
COPY frontend/package.json frontend/package-lock.json ./

RUN npm ci --no-audit --prefer-offline

# Copy source files (node_modules already installed above, excluded via .dockerignore)
COPY frontend/src ./src
COPY frontend/index.html ./index.html
COPY frontend/vite.config.ts ./vite.config.ts
COPY frontend/tsconfig.json ./tsconfig.json
COPY frontend/tsconfig.app.json ./tsconfig.app.json
COPY frontend/tsconfig.node.json ./tsconfig.node.json
COPY frontend/components.json ./components.json

# Build for production
ARG VITE_API_URL=""
ARG VITE_KEYCLOAK_URL="http://localhost:9090"
ARG VITE_KEYCLOAK_REALM="pizzaflow"
ARG VITE_KEYCLOAK_CLIENT_ID="pizzaflow-web"
ARG VITE_MAPBOX_TOKEN=""

ENV VITE_API_URL=${VITE_API_URL}
ENV VITE_KEYCLOAK_URL=${VITE_KEYCLOAK_URL}
ENV VITE_KEYCLOAK_REALM=${VITE_KEYCLOAK_REALM}
ENV VITE_KEYCLOAK_CLIENT_ID=${VITE_KEYCLOAK_CLIENT_ID}
ENV VITE_MAPBOX_TOKEN=${VITE_MAPBOX_TOKEN}

RUN npm run build:docker

# ── Stage 2: Serve ────────────────────────────────────────────────────────────
FROM nginx:1.27-alpine AS runtime

RUN rm -rf /usr/share/nginx/html/*

COPY --from=builder /app/dist /usr/share/nginx/html
COPY frontend/nginx-main.conf /etc/nginx/nginx.conf
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Allow nginx user to write logs and serve files
RUN chown -R nginx:nginx \
        /usr/share/nginx/html \
        /var/cache/nginx \
        /var/log/nginx \
        /etc/nginx/conf.d

USER nginx

EXPOSE 80

