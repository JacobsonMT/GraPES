package com.jacobsonmt.mags.server.dao;

import com.jacobsonmt.mags.server.entities.JobResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobResultDao extends JpaRepository<JobResult, Long> {

}
