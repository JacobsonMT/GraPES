package com.jacobsonmt.mags.server.dao;

import com.jacobsonmt.mags.server.entities.MaGSMarker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaGSMarkerDao extends JpaRepository<MaGSMarker, String> {
}
