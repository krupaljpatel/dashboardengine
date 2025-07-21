package com.dashboardengine.consumer.leadership;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadershipRepository extends JpaRepository<LeadershipRecord, String> {
    LeadershipRecord findByTaskName(String taskName);
}