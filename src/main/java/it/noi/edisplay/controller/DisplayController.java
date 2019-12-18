package it.noi.edisplay.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import it.noi.edisplay.dto.DisplayDto;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.repositories.DisplayRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Controller class to create API for CRUD operations on Displays
 */
@RestController
@RequestMapping("/display")
public class DisplayController {


    @Autowired
    DisplayRepository displayRepository;

    @Autowired
    ModelMapper modelMapper;

    @RequestMapping(value = "/get/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<DisplayDto> getDisplay(@PathVariable("uuid") String uuid) {
        Display display = displayRepository.findByUuid(uuid);

        if (display == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(modelMapper.map(display, DisplayDto.class), HttpStatus.OK);
    }


    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseEntity getAllDisplays() {
        return new ResponseEntity<>(modelMapper.map(displayRepository.findAll(), List.class), HttpStatus.OK);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity createDisplay(@RequestBody DisplayDto displayDto) {
        Display display = modelMapper.map(displayDto, Display.class);
        return new ResponseEntity<>(modelMapper.map(displayRepository.saveAndFlush(display), DisplayDto.class), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/delete/{uuid}", method = RequestMethod.DELETE)
    public ResponseEntity deleteDisplay(@PathVariable("uuid") String uuid) {
        Display display = displayRepository.findByUuid(uuid);

        if (display == null)
            return new ResponseEntity(HttpStatus.BAD_REQUEST);

        try {
            displayRepository.delete(display);
        } catch (EmptyResultDataAccessException ex) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);

    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = "application/json")
    public ResponseEntity updateDisplay(@RequestBody DisplayDto displayDto) {
        Display display = displayRepository.findByUuid(displayDto.getUuid());
        if (display == null)
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        display.setBatteryPercentage(displayDto.getBatteryPercentage());
        display.setImage(displayDto.getImage());
        display.setImage(displayDto.getImage());
        display.setName(displayDto.getName());
        display.setLastState(displayDto.getLastState());
        displayRepository.save(display);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

}
