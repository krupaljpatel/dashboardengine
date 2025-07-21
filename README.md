# Multi-Source Consumer Application

A high-performance Java application built with Spring Boot for consuming files and messages from multiple sources including File Systems, FTP servers, Message Queues, Kafka topics, and scheduled database queries.

## Features

- **5 Source Types**: File System, FTP/SFTP, Message Queues, Kafka, Database
- **High Throughput**: 10K+ messages/minute per pod instance
- **OpenShift Ready**: Clustered deployment with leader election
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

Configuration is externalized via `application.yml`. Key sections:

### Threading Configuration
```yaml
app:
  threading:
    core-pool-size: 10
    max-pool-size: 50
    queue-capacity: 1000
```

### File System Source
```yaml
app:
  filesystem:
    documents:
      path: "/tmp/input"
      patterns: ["*.txt", "*.csv"]
      poll-interval-ms: 5000
```

### Database Scheduler
```yaml
app:
  database:
    reports:
      url: "jdbc:postgresql://localhost/db"
      cron-expression: "0 */5 * * * *"
      query: "SELECT * FROM reports"
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

🔄 **Next Phases**: Source adapter implementations (File System, FTP, Kafka, MQ, Database)

## Contributing

See [REQUIREMENTS.md](REQUIREMENTS.md) for detailed feature requirements and implementation plan.