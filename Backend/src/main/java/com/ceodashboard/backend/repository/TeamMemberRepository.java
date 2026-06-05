package com.ceodashboard.backend.repository;

import com.ceodashboard.backend.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByProjectIdAndOrgId(Long projectId, Integer orgId);

    void deleteByProjectId(Long projectId);

    Optional<TeamMember> findByProjectIdAndMemberFirstNameAndMemberLastNameAndOrgId(
            Long projectId,
            String memberFirstName,
            String memberLastName,
            Integer orgId);

    long countByProjectIdAndOrgId(Long projectId, Integer orgId);
}
