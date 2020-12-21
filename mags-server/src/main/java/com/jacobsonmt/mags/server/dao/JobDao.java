package com.jacobsonmt.mags.server.dao;

import com.jacobsonmt.mags.server.entities.Job;
import com.jacobsonmt.mags.server.entities.Job.Status;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface JobDao extends JpaRepository<Job, Long> {

    @Transactional
    @Modifying
    @Query("update Job j set j.status = ?2 where j.id = ?1")
    void updateStatus(long id, Status status);

    @Transactional
    @Modifying
    @Query("update Job j set j.status = 'STOPPED', j.deleted=true where j.id = ?1")
    void stop(long id);

    @Transactional
    @Modifying
    @Query("update Job j set j.deleted=true where j.id = ?1")
    void delete(long id);

    List<Job> findBySession(String session);

    Optional<Job> findFirstBySessionNotInAndStatusIsOrderByCreatedDateAsc(Set<String> sessions, Status status);

}
