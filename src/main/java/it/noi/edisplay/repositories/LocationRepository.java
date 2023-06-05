// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.repositories;

import it.noi.edisplay.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JpaRepository to create CRUD operations on Location Entity
 */
public interface LocationRepository extends JpaRepository<Location, Integer> {


    Location findByUuid(String uuid);
    Location findByName(String name);

    List<Location> findAll();
}
