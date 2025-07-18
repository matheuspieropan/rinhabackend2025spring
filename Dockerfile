FROM alpine:3.19

RUN apk add --no-cache libc6-compat

WORKDIR /app

COPY target/rinha2025spring /app/rinha2025spring

RUN chmod +x /app/rinha2025spring

EXPOSE 8080

ENTRYPOINT ["./rinha2025spring"]