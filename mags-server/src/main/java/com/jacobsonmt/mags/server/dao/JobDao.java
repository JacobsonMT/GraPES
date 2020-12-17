package com.jacobsonmt.mags.server.dao;

import com.jacobsonmt.mags.server.entities.Job;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobDao extends JpaRepository<Job, Long> {

    Job findByJobKey(String job);

    @Query("select job.id from Job job where job.jobKey=?1")
    Long findIdByJobKey(String job);

    void deleteByJobKey(String job);

    List<Job> findByClientIdAndUserId(String client, String user);
}
