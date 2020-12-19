package com.jacobsonmt.mags.server.dao;

import com.jacobsonmt.mags.server.entities.PrecomputedResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PrecomputedResultDao extends JpaRepository<PrecomputedResult, String>, JpaSpecificationExecutor<PrecomputedResult> {

}
