// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.noi.edisplay.dto.ResolutionDto;
import it.noi.edisplay.services.ResolutionService;

/**
 * Controller class to create API for CRUD operations on Resolutions
 */
@RestController
@RequestMapping("/resolution")
public class ResolutionController {

    private final ResolutionService resolutionService;
    private final Logger logger = LoggerFactory.getLogger(ResolutionController.class);

    public ResolutionController(ResolutionService resolutionService) {
        this.resolutionService = resolutionService;
    }

    @GetMapping("/all")
    public ResponseEntity<ArrayList<ResolutionDto>> getAllResolutions() {
        try {
            ArrayList<ResolutionDto> dtoList = resolutionService.getAllResolutions();
            logger.debug("All resolutions requested");
            return new ResponseEntity<>(dtoList, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching resolutions", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
