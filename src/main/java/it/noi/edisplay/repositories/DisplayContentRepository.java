package it.noi.edisplay.repositories;

import it.noi.edisplay.model.DisplayContent;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaRepository to create CRUD operations on Display Content Entity
 */
public interface DisplayContentRepository extends JpaRepository<DisplayContent, Integer> {

    DisplayContent findByUuid(String uuid);

}
