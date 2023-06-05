// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.repositories;

import it.noi.edisplay.model.Display;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JpaRepository to create CRUD operations on Display Entity
 */
public interface DisplayRepository extends JpaRepository<Display, Integer> {

    Display findByUuid(String uuid);
    Display findByName(String name);
    List<Display> findAll();
}
