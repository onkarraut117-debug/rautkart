# ---- build stage -------------------------------------------------------
FROM node:20-alpine AS build
WORKDIR /app

# Lockfile first, so npm ci is cached until dependencies actually change.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---- runtime stage -----------------------------------------------------
# A React build is just static files. nginx serves them and proxies /api to
# the backend, which is what the Vite dev proxy does during development.
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
