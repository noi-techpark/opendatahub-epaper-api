// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.services;

import java.util.ArrayList;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import it.noi.edisplay.dto.ResolutionDto;
import it.noi.edisplay.model.Resolution;
import it.noi.edisplay.repositories.ResolutionRepository;

/**
 * Service class for Resolution Controller business logic.
 */
@Service
public class ResolutionService {

    private final ResolutionRepository resolutionRepository;
    private final ModelMapper modelMapper;

    public ResolutionService(ResolutionRepository resolutionRepository, ModelMapper modelMapper) {
        this.resolutionRepository = resolutionRepository;
        this.modelMapper = modelMapper;
    }

    public ArrayList<ResolutionDto> getAllResolutions() {
        List<Resolution> list = resolutionRepository.findAll();
        ArrayList<ResolutionDto> dtoList = new ArrayList<>();
        for (Resolution resolution : list) {
            dtoList.add(modelMapper.map(resolution, ResolutionDto.class));
        }
        return dtoList;
    }
}