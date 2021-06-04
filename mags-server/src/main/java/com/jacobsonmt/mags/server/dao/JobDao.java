package com.jacobsonmt.mags.server.dao;

import com.jacobsonmt.mags.server.entities.Job;
import com.jacobsonmt.mags.server.entities.Job.Status;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobDao extends JpaRepository<Job, Long> {

    List<Job> findBySessionAndDeletedFalseOrderByCreatedDateDesc(String session);

    Optional<Job> findFirstBySessionNotInAndStatusIsAndDeletedFalseOrderByCreatedDateAsc(Set<String> sessions, Status status);

    Optional<Job> findFirstByInputAndStatusAndInvalidatedFalseOrderByCreatedDateDesc(String input, Status status);

    Optional<Job> findByIdAndDeletedFalse(long id);

    long countJobByDeletedFalseAndStatusIn(Set<Status> statuses);
    long countJobBySessionIsAndDeletedFalseAndStatusIn(String session, Set<Status> statuses);
}
