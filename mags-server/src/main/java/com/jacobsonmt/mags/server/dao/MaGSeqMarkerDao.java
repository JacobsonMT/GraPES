package com.jacobsonmt.mags.server.dao;

import com.jacobsonmt.mags.server.entities.MaGSeqMarker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaGSeqMarkerDao extends JpaRepository<MaGSeqMarker, String> {
}
