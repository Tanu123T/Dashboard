package com.ceodashboard.backend.repository;

import com.ceodashboard.backend.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findBySprintId(Long sprintId);
    
    void deleteBySprintId(Long sprintId);
    
    Optional<TeamMember> findBySprintIdAndName(Long sprintId, String name);
    
    long countBySprintId(Long sprintId);
}
