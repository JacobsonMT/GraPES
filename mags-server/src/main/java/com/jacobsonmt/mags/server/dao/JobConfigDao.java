package com.jacobsonmt.mags.server.dao;

import com.jacobsonmt.mags.server.entities.JobConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobConfigDao extends JpaRepository<JobConfig, String> {

}
