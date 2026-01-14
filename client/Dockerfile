
FROM node:20-bullseye-slim AS deps
WORKDIR /app

# Copiar sólo los package.json para aprovechar cache de docker
COPY package.json package-lock.json* ./

# Instala dependencias (incluyendo dev deps necesarias para el build)
RUN npm ci

FROM node:20-bullseye-slim AS builder
WORKDIR /app

# Reusar node_modules ya instalados
COPY --from=deps /app/node_modules ./node_modules

# Copiar el resto del proyecto
COPY . .

# Construir la app Next.js
RUN npm run build

FROM node:20-bullseye-slim AS runner
WORKDIR /app

# Configuración de producción
ENV NODE_ENV=production
ENV PORT=3000

# Copiar package.json y reinstalar solo dependencias de producción
COPY package.json package-lock.json* ./
RUN npm ci --omit=dev --no-audit --no-fund

# Copiar artefactos del build
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public

EXPOSE 3000

# Iniciar el servidor Next.js
CMD ["npm", "start"]

