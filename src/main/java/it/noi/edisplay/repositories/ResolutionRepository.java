// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.repositories;

import it.noi.edisplay.model.Resolution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResolutionRepository extends JpaRepository<Resolution, Integer> {


	Resolution findByUuid(String uuid);
	Resolution findByWidthAndHeightAndBitDepth(int width, int height, int bitDepth);

	List<Resolution> findAll();
}
