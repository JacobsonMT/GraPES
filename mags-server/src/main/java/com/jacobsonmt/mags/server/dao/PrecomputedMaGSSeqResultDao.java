package com.jacobsonmt.mags.server.dao;

import com.jacobsonmt.mags.server.entities.PrecomputedMaGSSeqResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrecomputedMaGSSeqResultDao extends JpaRepository<PrecomputedMaGSSeqResult, String> {

    public List<PrecomputedMaGSSeqResult> findByMarkerTrue();
}
