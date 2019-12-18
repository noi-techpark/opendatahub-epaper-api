package it.noi.edisplay.repositories;

import it.noi.edisplay.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JpaRepository to create CRUD operations on Template Entity
 */
public interface TemplateRepository extends JpaRepository<Template, Integer> {


    Template findByUuid(String uuid);

    List<Template> findAll();
}
