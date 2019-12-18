package it.noi.edisplay.repositories;

import it.noi.edisplay.model.Display;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JpaRepository to create CRUD operations on Display Entity
 */
public interface DisplayRepository extends JpaRepository<Display, Long> {

    Display findByUuid(String uuid);
    List<Display> findAll();
}
