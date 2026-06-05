package com.ceodashboard.backend.repository;

import com.ceodashboard.backend.entity.SprintTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintTaskRepository extends JpaRepository<SprintTask, Long> {
    List<SprintTask> findBySprintId(Long sprintId);

    void deleteBySprintId(Long sprintId);

    long countBySprintId(Long sprintId);

    long countBySprintIdAndStatus(Long sprintId, String status);

    List<SprintTask> findBySprintIdAndAssignee(Long sprintId, String assignee);

    List<SprintTask> findBySprintIdAndAssignedMemberId(Long sprintId, Long assignedMemberId);
}
