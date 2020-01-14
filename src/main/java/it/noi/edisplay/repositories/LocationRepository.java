package it.noi.edisplay.repositories;

import it.noi.edisplay.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JpaRepository to create CRUD operations on Location Entity
 */
public interface LocationRepository extends JpaRepository<Location, Integer> {


    Location findByUuid(String uuid);

    List<Location> findAll();
}
g