package com.dashboardengine.consumer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {
    
    private Threading threading = new Threading();
    private Leadership leadership = new Leadership();
    private Map<String, FileSystemConfig> filesystem;
    private Map<String, FtpConfig> ftp;
    private Map<String, DatabaseConfig> database;
    private Map<String, KafkaConfig> kafka;
    private Map<String, MqConfig> mq;

    public static class Threading {
        private int corePoolSize = 10;
        private int maxPoolSize = 50;
        private int queueCapacity = 1000;
        private int keepAliveSeconds = 60;
        
        // getters and setters
        public int getCorePoolSize() { return corePoolSize; }
        public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }
        public int getMaxPoolSize() { return maxPoolSize; }
        public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
        public int getQueueCapacity() { return queueCapacity; }
        public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
        public int getKeepAliveSeconds() { return keepAliveSeconds; }
        public void setKeepAliveSeconds(int keepAliveSeconds) { this.keepAliveSeconds = keepAliveSeconds; }
    }
    
    public static class Leadership {
        private boolean enabled = true;
        private int heartbeatIntervalMs = 5000;
        private int leaderTimeoutMs = 15000;
        
        // getters and setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getHeartbeatIntervalMs() { return heartbeatIntervalMs; }
        public void setHeartbeatIntervalMs(int heartbeatIntervalMs) { this.heartbeatIntervalMs = heartbeatIntervalMs; }
        public int getLeaderTimeoutMs() { return leaderTimeoutMs; }
        public void setLeaderTimeoutMs(int leaderTimeoutMs) { this.leaderTimeoutMs = leaderTimeoutMs; }
    }
    
    public static class FileSystemConfig {
        private String path;
        private List<String> patterns;
        private int pollIntervalMs = 5000;
        private String archiveDir;
        private boolean deleteAfterProcess = false;
        
        // getters and setters
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public List<String> getPatterns() { return patterns; }
        public void setPatterns(List<String> patterns) { this.patterns = patterns; }
        public int getPollIntervalMs() { return pollIntervalMs; }
        public void setPollIntervalMs(int pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }
        public String getArchiveDir() { return archiveDir; }
        public void setArchiveDir(String archiveDir) { this.archiveDir = archiveDir; }
        public boolean isDeleteAfterProcess() { return deleteAfterProcess; }
        public void setDeleteAfterProcess(boolean deleteAfterProcess) { this.deleteAfterProcess = deleteAfterProcess; }
    }
    
    public static class FtpConfig {
        private String host;
        private int port = 21;
        private String username;
        private String password;
        private String directory;
        private List<String> patterns;
        private boolean secure = false;
        private int pollIntervalMs = 10000;
        
        // getters and setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getDirectory() { return directory; }
        public void setDirectory(String directory) { this.directory = directory; }
        public List<String> getPatterns() { return patterns; }
        public void setPatterns(List<String> patterns) { this.patterns = patterns; }
        public boolean isSecure() { return secure; }
        public void setSecure(boolean secure) { this.secure = secure; }
        public int getPollIntervalMs() { return pollIntervalMs; }
        public void setPollIntervalMs(int pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }
    }
    
    public static class DatabaseConfig {
        private String url;
        private String username;
        private String password;
        private String query;
        private String cronExpression;
        private int fetchSize = 1000;
        private String outputFormat = "JSON";
        private String outputPath;
        
        // getters and setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public String getCronExpression() { return cronExpression; }
        public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
        public int getFetchSize() { return fetchSize; }
        public void setFetchSize(int fetchSize) { this.fetchSize = fetchSize; }
        public String getOutputFormat() { return outputFormat; }
        public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
        public String getOutputPath() { return outputPath; }
        public void setOutputPath(String outputPath) { this.outputPath = outputPath; }
    }
    
    public static class KafkaConfig {
        private String bootstrapServers;
        private String topic;
        private String groupId;
        private int batchSize = 100;
        private boolean autoCommit = true;
        
        // getters and setters
        public String getBootstrapServers() { return bootstrapServers; }
        public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }
        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
        public boolean isAutoCommit() { return autoCommit; }
        public void setAutoCommit(boolean autoCommit) { this.autoCommit = autoCommit; }
    }
    
    public static class MqConfig {
        private String type; // rabbitmq, activemq, ibmmq
        private String host;
        private int port;
        private String username;
        private String password;
        private String queue;
        private int concurrency = 1;
        
        // getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getQueue() { return queue; }
        public void setQueue(String queue) { this.queue = queue; }
        public int getConcurrency() { return concurrency; }
        public void setConcurrency(int concurrency) { this.concurrency = concurrency; }
    }

    // Main class getters and setters
    public Threading getThreading() { return threading; }
    public void setThreading(Threading threading) { this.threading = threading; }
    public Leadership getLeadership() { return leadership; }
    public void setLeadership(Leadership leadership) { this.leadership = leadership; }
    public Map<String, FileSystemConfig> getFilesystem() { return filesystem; }
    public void setFilesystem(Map<String, FileSystemConfig> filesystem) { this.filesystem = filesystem; }
    public Map<String, FtpConfig> getFtp() { return ftp; }
    public void setFtp(Map<String, FtpConfig> ftp) { this.ftp = ftp; }
    public Map<String, DatabaseConfig> getDatabase() { return database; }
    public void setDatabase(Map<String, DatabaseConfig> database) { this.database = database; }
    public Map<String, KafkaConfig> getKafka() { return kafka; }
    public void setKafka(Map<String, KafkaConfig> kafka) { this.kafka = kafka; }
    public Map<String, MqConfig> getMq() { return mq; }
    public void setMq(Map<String, MqConfig> mq) { this.mq = mq; }
}