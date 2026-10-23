# ---------- Etapa 1: compilar la app ----------
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
# La URL de la API queda fija en el build de Vite
ARG VITE_API_URL=http://localhost:8080/api/v1
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

# ---------- Etapa 2: servir con Nginx ----------
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
