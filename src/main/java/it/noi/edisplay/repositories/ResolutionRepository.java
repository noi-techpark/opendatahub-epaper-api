package it.noi.edisplay.repositories;

import it.noi.edisplay.model.Resolution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResolutionRepository extends JpaRepository<Resolution, Integer> {


	Resolution findByUuid(String uuid);
	Resolution findByWidthAndHeightAndBitDepth(int width, int height, int bitDepth);

	List<Resolution> findAll();
}
