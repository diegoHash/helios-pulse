package com.helios.platform.pulse.repositories;

import com.helios.platform.pulse.entities.ClientLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientLogRepository extends JpaRepository<ClientLog, Long> {

    // Find logs by device alias, ordered by newest first
    List<ClientLog> findByDeviceAliasOrderByTimestampDesc(String deviceAlias);

    // Return unique devices (Alias, PC Name, IP, Device ID)
    @Query("SELECT c.deviceAlias, MAX(c.pcName), MAX(c.ipAddress), MAX(c.deviceId) FROM ClientLog c WHERE c.deviceAlias IS NOT NULL GROUP BY c.deviceAlias")
    List<Object[]> findDistinctDevices();

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM ClientLog c WHERE c.deviceAlias = :deviceAlias")
    void deleteByDeviceAlias(@Param("deviceAlias") String deviceAlias);
}
