package com.cognizant.agriserve.complianceservice.dao;

import com.cognizant.agriserve.complianceservice.entity.ComplianceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, Long> {


    List<ComplianceRecord> findByEntityId(Long entityId);

    List<ComplianceRecord> findByType(ComplianceRecord.ComplianceType type);

    List<ComplianceRecord> findByEntityIdAndType(Long entityId, ComplianceRecord.ComplianceType type);

    List<ComplianceRecord> findByOfficerId(Long officerId);

    List<ComplianceRecord> findAllByOrderByDateDesc();
}