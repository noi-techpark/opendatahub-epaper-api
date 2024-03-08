// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.controller;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.noi.edisplay.dto.ResolutionDto;
import it.noi.edisplay.model.Resolution;
import it.noi.edisplay.repositories.ResolutionRepository;

/**
 * Controller class to create API for CRUD operations on Resolutions
 */
@RestController
@RequestMapping("/resolution")
public class ResolutionController {

    @Autowired
    private ResolutionRepository resolutionRepository;

    @Autowired
    ModelMapper modelMapper;

    Logger logger = LoggerFactory.getLogger(ResolutionController.class);

    @GetMapping(value = "/all")
    public ResponseEntity<ArrayList<ResolutionDto>> getAllResolutions() {
        List<Resolution> list = resolutionRepository.findAll();
        ArrayList<ResolutionDto> dtoList = new ArrayList<>();
        for (Resolution resolution : list)
            dtoList.add(modelMapper.map(resolution, ResolutionDto.class));
        logger.debug("All resolutions requested");
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }
}
