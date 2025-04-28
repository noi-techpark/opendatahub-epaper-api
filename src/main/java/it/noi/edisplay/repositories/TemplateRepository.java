// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.repositories;

import it.noi.edisplay.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.List;

/**
 * JpaRepository to create CRUD operations on Template Entity
 */
public interface TemplateRepository extends JpaRepository<Template, Integer> {


    Template findByUuid(String uuid);
    Template findByName(String name);

    // List<Template> findAll();
}
