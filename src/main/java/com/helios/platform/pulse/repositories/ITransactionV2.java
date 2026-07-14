package com.helios.platform.pulse.repositories;

import com.helios.platform.pulse.entities.TransactionEntityV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITransactionV2 extends JpaRepository<TransactionEntityV2, Long> {

    //--------------------------------------------------------------------------------------------------------------
    // GENERAL REPORTS
    @Query("SELECT COALESCE(SUM(b.cantidad), 0) FROM TransactionEntityV2 t JOIN t.batches b WHERE t.deliveryDate BETWEEN :startDate AND :endDate")
    Integer sumCantidadByDeliveryDateBetween(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<TransactionEntityV2> findByDeliveryDateBetweenOrderByDeliveryDateAsc(String startDate, String endDate);

    //--------------------------------------------------------------------------------------------------------------
    // CUSTOM TYPE REPORTS
    @Query("SELECT COALESCE(SUM(b.cantidad), 0) FROM TransactionEntityV2 t JOIN t.batches b WHERE t.deliveryDate BETWEEN :startDate AND :endDate AND t.type = :type")
    Integer sumCantidadByDeliveryDateBetweenAndType(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("type") String type);

    List<TransactionEntityV2> findByDeliveryDateBetweenAndTypeOrderByDeliveryDateAsc(String startDate, String endDate, String type);

    //--------------------------------------------------------------------------------------------------------------
    // CUSTOM WORKSHIFT REPORTS
    @Query("SELECT COALESCE(SUM(b.cantidad), 0) FROM TransactionEntityV2 t JOIN t.batches b WHERE t.deliveryDate BETWEEN :startDate AND :endDate AND t.workshift = :workshift")
    Integer sumCantidadByDeliveryDateBetweenAndWorkshift(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("workshift") String workshift);

    List<TransactionEntityV2> findByDeliveryDateBetweenAndWorkshiftOrderByDeliveryDateAsc(String startDate, String endDate, String workshift);

    //--------------------------------------------------------------------------------------------------------------
    // CUSTOM REPORTS
    @Query("SELECT COALESCE(SUM(b.cantidad), 0) FROM TransactionEntityV2 t JOIN t.batches b WHERE t.deliveryDate BETWEEN :startDate AND :endDate AND t.type = :type AND t.workshift = :workshift")
    Integer sumCantidadByDeliveryDateBetweenAndTypeAndWorkshift(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("type") String type, @Param("workshift") String workshift);

    List<TransactionEntityV2> findByDeliveryDateBetweenAndTypeAndWorkshiftOrderByDeliveryDateAsc(String startDate, String endDate, String type, String workshift);

    //--------------------------------------------------------------------------------------------------------------
    // CUSTOM MULTI-TYPE REPORTS
    @Query("SELECT COALESCE(SUM(b.cantidad), 0) FROM TransactionEntityV2 t JOIN t.batches b WHERE t.deliveryDate BETWEEN :startDate AND :endDate AND t.type IN :types")
    Integer sumCantidadByDeliveryDateBetweenAndTypeIn(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("types") List<String> types);

    List<TransactionEntityV2> findByDeliveryDateBetweenAndTypeInOrderByDeliveryDateAsc(String startDate, String endDate, List<String> types);

    @Query("SELECT COALESCE(SUM(b.cantidad), 0) FROM TransactionEntityV2 t JOIN t.batches b WHERE t.deliveryDate BETWEEN :startDate AND :endDate AND t.type IN :types AND t.workshift = :workshift")
    Integer sumCantidadByDeliveryDateBetweenAndTypeInAndWorkshift(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("types") List<String> types, @Param("workshift") String workshift);

    List<TransactionEntityV2> findByDeliveryDateBetweenAndTypeInAndWorkshiftOrderByDeliveryDateAsc(String startDate, String endDate, List<String> types, String workshift);

    List<TransactionEntityV2> findByPropertyOrderByDeliveryDateDesc(String property);

    @Query("SELECT COALESCE(MAX(b.serialFinal), 0) FROM TransactionEntityV2 t JOIN t.batches b")
    Integer findMaxSerialFinal();

}
