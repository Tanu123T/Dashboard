package com.ceodashboard.backend.repository;

import com.ceodashboard.backend.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProjectId(Long projectId);
    
    List<Sprint> findByProjectIdOrderByStartDateDesc(Long projectId);
    
    long countByProjectId(Long projectId);
    
    long countByProjectIdAndStatus(Long projectId, String status);
    
    List<Sprint> findByProjectIdAndStatus(Long projectId, String status);
}
