package it.noi.edisplay.repositories;

import it.noi.edisplay.model.Connection;
import it.noi.edisplay.model.Display;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JpaRepository to create CRUD operations on Connection Entity
 */
public interface ConnectionRepository extends JpaRepository<Connection, Integer> {
    Connection findByUuid(String uuid);
    Connection findByDisplay(Display display);
    Connection findByMac(String mac);
    List<Connection> findAll();
}
