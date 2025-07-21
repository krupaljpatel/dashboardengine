# Multi-Source Consumer Application

A high-performance Java application built with Spring Boot for consuming files and messages from multiple sources including File Systems, FTP servers, Message Queues, Kafka topics, and scheduled database queries.

## Features

- **5 Source Types**: File System, FTP/SFTP, Message Queues, Kafka, Database
- **High Throughput**: 10K+ messages/minute per pod instance
- **OpenShift Ready**: Clustered deployment with leader election
- **REST API Configuration**: Dynamic configuration via REST endpoints
- **Isolated Processing**: Each file location runs independently with dedicated threads
- **Monitoring**: Prometheus metrics and health checks
- **Resilient**: Circuit breakers, retry mechanisms, error handling

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (for production)

### Running Locally
```bash
./mvnw spring-boot:run
```

### Running Tests
```bash
./mvnw test
```

### Building Docker Image
```bash
docker build -t multi-source-consumer .
```

## Configuration

### Static Configuration (application.yml)
Basic configuration via `application.yml` (auto-loaded at startup):

### Dynamic Configuration (REST API)
Runtime configuration via REST endpoints:

#### Create File System Configuration
```bash
curl -X POST http://localhost:8080/api/v1/filesystem/configs/documents \
  -H "Content-Type: application/json" \
  -d '{
    "path": "/tmp/input",
    "patterns": ["*.txt", "*.csv", "*.json"],
    "pollIntervalMs": 5000,
    "maxConcurrentFiles": 10,
    "deleteAfterProcess": false,
    "archiveDir": "/tmp/archive"
  }'
```

#### Start/Stop Consumer
```bash
# Start consumer
curl -X POST http://localhost:8080/api/v1/filesystem/configs/documents/start

# Stop consumer  
curl -X POST http://localhost:8080/api/v1/filesystem/configs/documents/stop

# Get status
curl -X GET http://localhost:8080/api/v1/filesystem/status/documents
```

#### Update Configuration (Hot Reload)
```bash
curl -X PUT http://localhost:8080/api/v1/filesystem/configs/documents \
  -H "Content-Type: application/json" \
  -d '{
    "path": "/tmp/input", 
    "patterns": ["*.txt"],
    "pollIntervalMs": 2000,
    "maxConcurrentFiles": 20
  }'
```

## Monitoring

- Health Checks: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/prometheus`
- API Documentation: `http://localhost:8080/swagger-ui.html`

## OpenShift Deployment

The application includes:
- Leader election for database scheduling
- Configuration via ConfigMaps and Secrets
- Horizontal Pod Autoscaling support
- Graceful shutdown handling

## Architecture

### Core Components
- **SourceAdapters**: Handle different source types
- **MessageProcessor**: Process messages/files
- **LeadershipService**: Cluster coordination
- **ProcessingMetrics**: Performance monitoring

### Thread Pool Management
- Dedicated thread pools for processing
- Configurable pool sizes and queue capacity
- Graceful shutdown with task completion

## Development Status

✅ **Phase 1 Complete**: Core Spring Boot framework, configuration, threading, leadership election, health checks, metrics

✅ **Phase 2 Complete**: File System Consumer with directory monitoring, pattern matching, concurrent processing

🔄 **Next Phases**: Database Scheduler, FTP Consumer, Kafka Consumer, MQ Consumer

## Contributing

See [REQUIREMENTS.md](REQUIREMENTS.md) for detailed feature requirements and implementation plan.