# ---- Dependencies & build ----
FROM node:22-alpine AS builder
WORKDIR /app

# 依存関係を先にコピーしてキャッシュ効率化
COPY package.json package-lock.json ./
RUN npm ci

# ソースをコピー
COPY . .

# Next.js の rewrite 先（ビルド時に埋め込む必要があるため ARG で受け取る）
ARG API_BASE_URL=http://api:8080
ENV API_BASE_URL=${API_BASE_URL}

ENV NEXT_TELEMETRY_DISABLED=1
RUN npm run build

# ---- Runner ----
FROM node:22-alpine
WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

COPY --from=builder /app/package*.json ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/templates ./templates
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/next.config.ts ./next.config.ts

EXPOSE 3000
CMD ["npm", "start"]
