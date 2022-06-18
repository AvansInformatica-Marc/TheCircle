# Build
FROM node:16-alpine as build

ARG THECIRCLE_HOST
ENV THECIRCLE_HOST=${THECIRCLE_HOST}

ARG THECIRCLE_PORT
ENV THECIRCLE_PORT=${THECIRCLE_PORT}

WORKDIR /usr/local/app

COPY . /usr/local/app/
RUN npm ci
RUN npm run build

# Run
FROM nginx:latest

COPY --from=build /usr/local/app/dist /usr/share/nginx/html

EXPOSE 80
