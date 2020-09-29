package it.noi.edisplay.controller;

import it.noi.edisplay.dto.TemplateDto;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.utils.ImageUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class to create API for CRUD operations on Templates
 */
@RestController
@RequestMapping("/template")
public class TemplateController {

	@Autowired
	TemplateRepository templateRepository;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	private ImageUtil imageUtil;

	Logger logger = LoggerFactory.getLogger(TemplateController.class);

	@RequestMapping(value = "/get/{uuid}", method = RequestMethod.GET)
	public ResponseEntity<TemplateDto> getTemplate(@PathVariable("uuid") String uuid) {
		Template template = templateRepository.findByUuid(uuid);

		if (template == null) {
			logger.debug("Template with uuid: " + uuid + " not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		logger.debug("Get display with uuid: " + uuid);
		return new ResponseEntity<>(modelMapper.map(template, TemplateDto.class), HttpStatus.OK);
	}

	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public ResponseEntity getAllTemplates() {
		List<Template> list = templateRepository.findAll();
		ArrayList<TemplateDto> dtoList = new ArrayList<>();
		for (Template template : list)
			dtoList.add(modelMapper.map(template, TemplateDto.class));
		logger.debug("All templates requested");
		return new ResponseEntity<>(dtoList, HttpStatus.OK);
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity createTemplate(@RequestParam("name") String name, @RequestParam("image") MultipartFile image) {
		Template template = new Template();
		template.setName(name);

		try {
			InputStream in = new ByteArrayInputStream(image.getBytes());
			BufferedImage bImageFromConvert = ImageIO.read(in);
			template.setImage(imageUtil.convertToMonochrome(bImageFromConvert));
		} catch (IOException e) {
			e.printStackTrace();
			logger.debug("Template with creation failed. Image creation error.");
		}

		Template savedTemplate = templateRepository.saveAndFlush(template);

		logger.debug("Template with uuid:" + savedTemplate.getUuid() + " created.");
		return new ResponseEntity<>(modelMapper.map(savedTemplate, TemplateDto.class), HttpStatus.CREATED);
	}

	@RequestMapping(value = "/delete/{uuid}", method = RequestMethod.DELETE)
	public ResponseEntity deleteTemplate(@PathVariable("uuid") String uuid) {
		Template template = templateRepository.findByUuid(uuid);

		if (template == null) {
			logger.debug("Delete template with uuid:" + uuid + " failed.");
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
		templateRepository.delete(template);
		logger.debug("Deleted template with uuid:" + uuid);
		return new ResponseEntity(HttpStatus.OK);

	}

	@RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = "application/json")
	public ResponseEntity updateTemplate(@RequestBody TemplateDto templateDto) {
		Template template = templateRepository.findByUuid(templateDto.getUuid());
		if (template == null) {
			logger.debug("Update template with uuid:" + templateDto.getUuid() + " failed.");
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
		template.setName(templateDto.getName());
		template.setImage(templateDto.getImage());
		templateRepository.saveAndFlush(template);
		logger.debug("Updated template with uuid:" + template.getUuid());
		return new ResponseEntity(HttpStatus.ACCEPTED);
	}
}

