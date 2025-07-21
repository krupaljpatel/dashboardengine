# Dashboard Engine - Multi-Source File Consumer Application

## Project Overview
A robust Java application that consumes files and messages from multiple sources including local/network file systems, FTP servers, message queues, Kafka topics, and database queries on a scheduled basis.

## Requirements

### Functional Requirements

#### 1. File System Consumer
- **FR-001**: Monitor local and network file system directories for new files
- **FR-002**: Support file watching with configurable polling intervals
- **FR-003**: Process files based on configurable patterns (wildcards, regex)
- **FR-004**: Handle file locking and concurrent access scenarios
- **FR-005**: Archive or delete processed files based on configuration
- **FR-006**: Support multiple directory monitoring concurrently
- **FR-007**: Handle network file system connectivity issues

#### 2. FTP File Consumer
- **FR-008**: Connect to FTP/SFTP servers using configurable credentials
- **FR-009**: Monitor specified directories for new files
- **FR-010**: Download files based on configurable patterns (wildcards, regex)
- **FR-011**: Process files immediately after download
- **FR-012**: Archive or delete processed files based on configuration
- **FR-013**: Handle connection failures and automatic reconnection
- **FR-014**: Support multiple FTP servers concurrently

#### 3. Message Queue (MQ) Consumer
- **FR-015**: Connect to IBM MQ, ActiveMQ, or RabbitMQ
- **FR-016**: Subscribe to specified queues
- **FR-017**: Process messages in real-time
- **FR-018**: Handle message acknowledgment and error scenarios
- **FR-019**: Support message filtering and routing
- **FR-020**: Dead letter queue handling for failed messages

#### 4. Kafka Message Consumer
- **FR-021**: Connect to Kafka clusters
- **FR-022**: Subscribe to specified topics and partitions
- **FR-023**: Process messages with configurable batch sizes
- **FR-024**: Handle offset management (manual/automatic)
- **FR-025**: Support consumer groups for scalability
- **FR-026**: Handle rebalancing and partition assignment

#### 5. Database SQL Scheduler
- **FR-027**: Execute SQL queries on scheduled intervals
- **FR-028**: Support multiple database types (MySQL, PostgreSQL, Oracle, SQL Server)
- **FR-029**: Export query results to files (CSV, JSON, XML)
- **FR-030**: Handle large result sets with pagination
- **FR-031**: Configure connection pooling
- **FR-032**: Support parameterized queries with dynamic values

#### 6. File Processing Engine
- **FR-033**: Parse various file formats (CSV, JSON, XML, TXT, Excel)
- **FR-034**: Validate file structure and content
- **FR-035**: Transform data using configurable rules
- **FR-036**: Route processed data to output destinations
- **FR-037**: Generate processing reports and metrics

#### 7. Configuration Management
- **FR-038**: Externalized configuration via properties/YAML files
- **FR-039**: Environment-specific configurations
- **FR-040**: Hot reload of configuration without restart
- **FR-041**: Encrypted storage of sensitive data (passwords, keys)

#### 8. Monitoring and Alerting
- **FR-042**: Health check endpoints
- **FR-043**: Metrics collection (processed files, messages, errors)
- **FR-044**: Logging with configurable levels
- **FR-045**: Alert notifications for failures
- **FR-046**: Dashboard for real-time monitoring

### Non-Functional Requirements

#### Performance
- **NFR-001**: Process at least 10,000 files/messages per minute per instance
- **NFR-002**: Support horizontal scaling across multiple OpenShift pods
- **NFR-003**: Memory usage optimization for large files (streaming processing)
- **NFR-004**: Database connection pooling (min 10, max 100 connections per instance)
- **NFR-005**: Sub-100ms processing latency for individual files/messages
- **NFR-006**: High throughput with concurrent processing (minimum 50 threads)
- **NFR-007**: Auto-scaling based on message queue depth and CPU utilization

#### Reliability
- **NFR-008**: 99.9% uptime availability in clustered OpenShift environment
- **NFR-009**: Automatic retry mechanisms with exponential backoff
- **NFR-010**: Circuit breaker patterns for external dependencies
- **NFR-011**: Data integrity and transaction management across cluster nodes
- **NFR-012**: Leader election for scheduled tasks (database queries) to prevent duplication
- **NFR-013**: Graceful shutdown and pod lifecycle management in OpenShift

#### Security
- **NFR-014**: Encrypted connections (SSL/TLS) for all external communications
- **NFR-015**: Authentication and authorization for management endpoints
- **NFR-016**: Audit logging for all operations
- **NFR-017**: Secure credential management using OpenShift secrets and config maps
- **NFR-018**: Network policies for pod-to-pod communication security

#### Maintainability
- **NFR-019**: Modular architecture with clear separation of concerns
- **NFR-020**: Comprehensive unit and integration tests (>80% coverage)
- **NFR-021**: API documentation with Swagger/OpenAPI
- **NFR-022**: OpenShift-ready containerization with health checks and resource limits
- **NFR-023**: Kubernetes/OpenShift deployment manifests with auto-scaling configurations

## Technical Architecture

### Technology Stack
- **Framework**: Spring Boot 3.x (standalone, no Spring Cloud)
- **Build Tool**: Maven
- **Java Version**: 17+ (OpenJDK)
- **Container Runtime**: OpenShift/Kubernetes with Red Hat UBI base images
- **Database**: PostgreSQL (production), H2 (development/testing)
- **Message Brokers**: Apache Kafka (clustered), RabbitMQ/ActiveMQ
- **FTP Client**: Apache Commons Net with connection pooling
- **Load Balancing**: OpenShift service mesh and ingress controllers
- **Testing**: JUnit 5, Testcontainers, Mockito, Spring Boot Test
- **Monitoring**: Micrometer, Actuator, Prometheus metrics
- **Documentation**: Swagger/OpenAPI
- **Configuration**: OpenShift ConfigMaps and Secrets
- **Logging**: Structured logging with JSON format

### Core Components

#### 1. Source Adapters
- `FileSystemSourceAdapter`: Monitors local and network file system directories
- `FtpSourceAdapter`: Handles FTP/SFTP connections and file monitoring
- `MqSourceAdapter`: Manages message queue connections and consumption
- `KafkaSourceAdapter`: Handles Kafka topic consumption
- `DatabaseSourceAdapter`: Executes scheduled SQL queries

#### 2. Processing Engine
- `FileProcessor`: Core file processing logic
- `MessageProcessor`: Message processing and routing
- `DataTransformer`: Data transformation utilities
- `ValidationService`: Data validation and quality checks

#### 3. Output Handlers
- `FileOutputHandler`: Writes processed data to files
- `DatabaseOutputHandler`: Inserts data into databases
- `MessageOutputHandler`: Publishes to message queues/topics

#### 4. Configuration Management
- `ConfigurationManager`: Handles configuration loading and hot reload
- `CredentialManager`: Secure credential storage and retrieval

#### 5. Monitoring and Health
- `HealthCheckService`: Application health monitoring
- `MetricsCollector`: Performance metrics collection
- `AlertService`: Notification and alerting system

## Implementation Phases

### Phase 1: Core Framework (Weeks 1-2)
- Set up Spring Boot project structure
- Implement configuration management
- Create base interfaces and abstractions
- Set up logging and basic monitoring

### Phase 2: FTP Integration (Week 3)
- Implement FTP/SFTP client
- File monitoring and processing
- Error handling and retry logic

### Phase 3: Database Scheduler (Week 4)
- Database connection management
- SQL execution scheduler
- Result export functionality

### Phase 4: Message Queue Integration (Week 5)
- MQ consumer implementation
- Message processing pipeline
- Dead letter queue handling

### Phase 5: Kafka Integration (Week 6)
- Kafka consumer setup
- Partition management
- Offset handling strategies

### Phase 6: Advanced Features (Weeks 7-8)
- File format parsers
- Data transformation engine
- Advanced monitoring and alerting
- Performance optimization

### Phase 7: Testing and Documentation (Weeks 9-10)
- Comprehensive test suite
- Performance testing
- API documentation
- Deployment guides

## Success Criteria
1. Successfully consume files from all four source types in clustered mode
2. Process 10,000+ files/messages per minute per pod instance
3. Zero data loss during processing with distributed transaction support
4. Sub-100ms response time for health checks and processing latency
5. Horizontal scaling demonstration (2-10 pods) based on load
6. Complete test coverage >80% including integration tests
7. OpenShift-ready containers with resource limits and health checks
8. Comprehensive monitoring with Prometheus metrics and alerting
9. Leader election working correctly for scheduled database tasks
10. Graceful shutdown and rolling updates without message loss

## Risks and Mitigation
1. **Network Connectivity**: Implement retry mechanisms and circuit breakers
2. **Data Volume**: Use streaming and batch processing techniques
3. **Security**: Encrypt all communications and secure credential storage
4. **Scalability**: Design for horizontal scaling from the start
5. **Maintenance**: Comprehensive logging and monitoring for troubleshooting