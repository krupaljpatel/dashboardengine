package com.dashboardengine.consumer.leadership;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leadership")
public class LeadershipRecord {

    @Id
    @Column(name = "task_name")
    private String taskName;

    @Column(name = "leader_id")
    private String leaderId;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
}