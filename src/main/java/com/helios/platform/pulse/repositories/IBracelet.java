package com.helios.platform.pulse.repositories;

import com.helios.platform.pulse.entities.BraceletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBracelet extends JpaRepository<BraceletEntity, Long> {

    List<BraceletEntity> findBySerialNumber(Integer serialNumber);

    List<BraceletEntity> findByMigratedToV2FalseOrMigratedToV2IsNull();

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE BraceletEntity b SET b.migratedToV2 = false")
    void resetMigrationFlags();

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE BraceletEntity b SET b.migratedToV2 = true WHERE b.id IN :ids")
    void markAsMigrated(@Param("ids") List<Long> ids);

    //--------------------------------------------------------------------------------------------------------------
    // GENERAL REPORTS
    Integer countByDeliveryDateBetween(String startDate, String endDate);

    List<BraceletEntity> findByDeliveryDateBetweenOrderBySerialNumberAsc(String startDate, String endDate);

    //--------------------------------------------------------------------------------------------------------------
    // CUSTOM TYPE REPORTS
    Integer countByDeliveryDateBetweenAndType(String startDate, String endDate, String type);

    List<BraceletEntity> findByDeliveryDateBetweenAndTypeOrderBySerialNumberAsc(String startDate, String endDate, String type);

    //--------------------------------------------------------------------------------------------------------------
    // CUSTOM WORKSHIFT REPORTS
    Integer countByDeliveryDateBetweenAndWorkshift(String startDate, String endDate, String workshift);

    List<BraceletEntity> findByDeliveryDateBetweenAndWorkshiftOrderBySerialNumberAsc(String startDate, String endDate, String workshift);

    //--------------------------------------------------------------------------------------------------------------
    // CUSTOM REPORTS
    Integer countByDeliveryDateBetweenAndTypeAndWorkshift(String startDate, String endDate, String type, String workshift);

    List<BraceletEntity> findByDeliveryDateBetweenAndTypeAndWorkshiftOrderBySerialNumberAsc(String startDate, String endDate, String type, String workshift);

}
