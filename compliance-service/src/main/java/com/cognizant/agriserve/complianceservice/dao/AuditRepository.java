package com.cognizant.agriserve.complianceservice.dao;

import com.cognizant.agriserve.complianceservice.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {


    List<Audit> findByOfficerId(Long officerId);

    List<Audit> findByStatus(Audit.AuditStatus status);

    List<Audit> findByOfficerIdAndStatus(Long officerId, Audit.AuditStatus status);

    List<Audit> findAllByOrderByDateDesc();
}