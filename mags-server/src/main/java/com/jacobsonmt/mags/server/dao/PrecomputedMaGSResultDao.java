package com.jacobsonmt.mags.server.dao;

import com.jacobsonmt.mags.server.entities.PrecomputedMaGSResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PrecomputedMaGSResultDao extends JpaRepository<PrecomputedMaGSResult, String>, JpaSpecificationExecutor<PrecomputedMaGSResult> {

}
