// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.controller;

import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.DisplayDto;
import it.noi.edisplay.dto.DisplayStateDto;
import it.noi.edisplay.services.DisplayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Controller class to create API for CRUD operations on Displays
 */
@RestController
@RequestMapping("/display")
public class DisplayController {
	
	private DisplayService displayService;

	public DisplayController(DisplayService displayService) {
		this.displayService = displayService;
	}

	@GetMapping("/get/{displayUuid}")
	public ResponseEntity<DisplayDto> getDisplay(@PathVariable("displayUuid") String uuid) {
		return displayService.getDisplay(uuid);
	}

	@GetMapping("/all")
	public ResponseEntity<List<DisplayDto>> getAllDisplays() {
		return displayService.getAllDisplays();
	}

	@PostMapping(value = "/create", consumes = "application/json")
	public ResponseEntity<Object> createDisplay(@RequestBody DisplayDto displayDto) {
		return displayService.createDisplay(displayDto);
	}

	@DeleteMapping("/delete/{displayUuid}")
	public ResponseEntity<?> deleteDisplay(@PathVariable("displayUuid") String displayUuid) {
		return displayService.deleteDisplay(displayUuid);
	}

	@PutMapping(value = "/update")
	public ResponseEntity<Object> updateDisplay(@RequestBody DisplayDto displayDto) {
		return displayService.updateDisplay(displayDto);
	}

	@PostMapping(value = "/sync-status/{displayUuid}", consumes = "application/json")
	public ResponseEntity<String> syncDisplayStatus(
			@PathVariable("displayUuid") String displayUuid,
			@RequestBody DisplayStateDto stateDto) throws IOException, NoSuchAlgorithmException {
		return displayService.syncDisplayStatus(displayUuid, stateDto);

	}

	@GetMapping(value = "/get-image/{displayUuid}")
	public ResponseEntity<byte[]> getDisplayImage(
			@PathVariable("displayUuid") String displayUuid,
			@RequestParam(value = "convertToBMP", required = false) boolean convertToBMP, 
			@RequestParam(value = "withTextFields", required = false, defaultValue = "false") boolean withTextFields) 
			throws IOException, NoSuchAlgorithmException {
		return displayService.getDisplayImage(displayUuid, convertToBMP, withTextFields);
	}

	@PostMapping(value = "/set-new-image/{displayUuid}", consumes = "multipart/form-data")
	public ResponseEntity<DisplayContentDto> setDisplayContent(
			@PathVariable("displayUuid") String displayUuid,
			@RequestParam("displayContentDtoJson") String displayContentDtoJson,
			@RequestParam(value = "image", required = false) MultipartFile image) 
			throws IOException {
		return displayService.setDisplayContent(displayUuid, displayContentDtoJson, image);
	}

	@PostMapping(value = "/set-template-image/{displayUuid}")
	public ResponseEntity<DisplayContentDto> setDisplayContentByTemplate(
			@PathVariable("displayUuid") String displayUuid, 
			@RequestParam("templateUuid") String templateUuid,
			@RequestBody DisplayContentDto displayContentDto) 
			throws JsonProcessingException {
		return displayService.setDisplayContentByTemplate(displayUuid, templateUuid, displayContentDto);
	}
}
