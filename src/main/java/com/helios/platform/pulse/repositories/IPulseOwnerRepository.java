package com.helios.platform.pulse.repositories;

import jakarta.transaction.Transactional;
import com.helios.platform.pulse.entities.PropietarioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public interface IPulseOwnerRepository extends JpaRepository<PropietarioModel, Long> {

    @Query("SELECT p.numProperty, p.idPropietario FROM PropietarioModel p")
    List<Object[]> findPropertiesData();

    default HashMap<String, Long> findProperties() {
        List<Object[]> data = findPropertiesData();
        HashMap<String, Long> result = new HashMap<>();
        for (Object[] obj : data) {
            result.put((String) obj[0], (Long) obj[1]);
        }
        return result;
    }

    @Query("SELECT COUNT(p.numProperty) FROM PropietarioModel p")
    Long countProperties();

    @Modifying
    @Transactional
    @Query("UPDATE PropietarioModel p SET p.cupoRestanteBrazalete = p.cupoBrazalete")
    void resetAll();

    @Modifying
    @Transactional
    @Query("UPDATE PropietarioModel p SET p.cupoRestanteBrazalete = p.cupoRestanteBrazalete - :value WHERE p.idPropietario = :id")
    int deductCupo(@Param("id") Long id, @Param("value") int value);

    @Modifying
    @Transactional
    @Query("UPDATE PropietarioModel p SET p.telefono = :telefono WHERE p.idPropietario = :id")
    int updateTelefono(@Param("id") Long id, @Param("telefono") String telefono);
}
