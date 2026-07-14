package com.helios.platform.pulse.repositories;

import com.helios.platform.pulse.entities.LoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ILoteRepo extends JpaRepository<LoteEntity, Long> {

    @Query("SELECT l FROM LoteEntity l WHERE l.type = :type AND l.firstSerial <= :serial AND l.lastSerial >= :serial")
    List<LoteEntity> findLoteBySerialAndType(@Param("serial") Integer serial, @Param("type") String type);

    @Query("SELECT COALESCE(SUM(l.remaining), 0) FROM LoteEntity l")
    Integer sumRemaining();

}
