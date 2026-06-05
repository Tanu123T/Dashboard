package com.ceodashboard.backend.repository;

import com.ceodashboard.backend.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProjectIdAndOrgId(Long projectId, Integer orgId);

    List<Sprint> findByProjectIdAndOrgIdOrderByStartDateDesc(Long projectId, Integer orgId);

    List<Sprint> findByOrgId(Integer orgId);

    long countByProjectIdAndOrgId(Long projectId, Integer orgId);

    long countByProjectIdAndOrgIdAndStatus(Long projectId, Integer orgId, String status);

    List<Sprint> findByProjectIdAndOrgIdAndStatus(Long projectId, Integer orgId, String status);
}
