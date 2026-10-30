FROM python:3.12-slim-bookworm

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PYTHONPATH=/app:/app/gen

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY gen ./gen
COPY telemetry ./telemetry

# 8083 - HTTP health, 9091 - gRPC
EXPOSE 8083 9091

CMD ["python", "-m", "telemetry"]
