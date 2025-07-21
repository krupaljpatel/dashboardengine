package com.dashboardengine.consumer.leadership;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LeadershipService {

    private static final Logger logger = LoggerFactory.getLogger(LeadershipService.class);
    
    private final LeadershipRepository repository;
    private final String instanceId;
    private volatile boolean isLeader = false;

    public LeadershipService(LeadershipRepository repository) {
        this.repository = repository;
        this.instanceId = UUID.randomUUID().toString();
        logger.info("Leadership service initialized with instance ID: {}", instanceId);
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void maintainLeadership() {
        try {
            LeadershipRecord current = repository.findByTaskName("database-scheduler");
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiry = now.minusSeconds(15); // 15 second timeout

            if (current == null || current.getLastHeartbeat().isBefore(expiry)) {
                // Claim or reclaim leadership
                if (current == null) {
                    current = new LeadershipRecord();
                    current.setTaskName("database-scheduler");
                }
                current.setLeaderId(instanceId);
                current.setLastHeartbeat(now);
                repository.save(current);
                
                if (!isLeader) {
                    logger.info("Claimed leadership for database-scheduler");
                    isLeader = true;
                }
            } else if (instanceId.equals(current.getLeaderId())) {
                // Update heartbeat if we're the leader
                current.setLastHeartbeat(now);
                repository.save(current);
                isLeader = true;
            } else {
                // Someone else is leader
                isLeader = false;
            }
        } catch (Exception e) {
            logger.error("Error maintaining leadership", e);
            isLeader = false;
        }
    }

    public boolean isLeader() {
        return isLeader;
    }

    public String getInstanceId() {
        return instanceId;
    }
}