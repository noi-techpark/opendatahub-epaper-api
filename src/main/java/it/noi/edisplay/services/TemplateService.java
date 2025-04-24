package it.noi.edisplay.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.modelmapper.ModelMapper;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.ResolutionDto;
import it.noi.edisplay.dto.TemplateDto;
import it.noi.edisplay.model.DisplayContent;
import it.noi.edisplay.model.Resolution;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.ResolutionRepository;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.storage.FileImportStorageS3;
import it.noi.edisplay.utils.ImageUtil;

/**
 * Servce class to Template Controller business logic
 */
@Service
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final ModelMapper modelMapper;
    private final ResolutionRepository resolutionRepository;
    private final ImageUtil imageUtil;
    private final FileImportStorageS3 fileImportStorageS3;

    public TemplateService(
        TemplateRepository templateRepository,
        ModelMapper modelMapper,
        ResolutionRepository resolutionRepository,
        ImageUtil imageUtil,
        FileImportStorageS3 fileImportStorageS3
    ) {
        this.templateRepository = templateRepository;
        this.modelMapper = modelMapper;
        this.resolutionRepository = resolutionRepository;
        this.imageUtil = imageUtil;
        this.fileImportStorageS3 = fileImportStorageS3;
    }

    // private final Logger logger = LoggerFactory.getLogger(TemplateService.class);

    public TemplateDto getTemplateByUuid(String uuid) {
        Template template = templateRepository.findByUuid(uuid);
        return template == null ? null : modelMapper.map(template, TemplateDto.class);
    }

    public List<TemplateDto> getAllTemplates() {
        List<Template> templates = templateRepository.findAll();
        List<TemplateDto> templateDtos = new ArrayList<>();
        for (Template t : templates) {
            templateDtos.add(modelMapper.map(t, TemplateDto.class));
        }
        return templateDtos;
    }

    public TemplateDto createTemplate(TemplateDto templateDto) {
        Template template = modelMapper.map(templateDto, Template.class);
        ResolutionDto resolutionDto = templateDto.getResolution();

        if (resolutionDto != null) {
            Resolution resolution = resolutionRepository.findByWidthAndHeightAndBitDepth(
                resolutionDto.getWidth(), 
                resolutionDto.getHeight(), 
                resolutionDto.getBitDepth());

            if (resolution == null) {
                throw new IllegalArgumentException("Resolution does not exist!");
            }

            template.setResolution(resolution);
        } else {
            throw new IllegalArgumentException("Resolution is required");
        }

        try {
            template = templateRepository.saveAndFlush(template);
        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause != null) {
                throw new IllegalArgumentException(rootCause.getMessage());
            } else {
                throw(e);
            }
        }

        return modelMapper.map(template, TemplateDto.class);
        
    }

    public void updateTemplate(TemplateDto templateDto) {
        Template template = templateRepository.findByUuid(templateDto.getUuid());
        if (template == null) {
            throw new IllegalArgumentException("Template not found!");
        }
        template.setName(templateDto.getName());
        template.setDescription(templateDto.getDescription());
        try {
            templateRepository.saveAndFlush(template);
        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause != null) {
                throw new IllegalArgumentException(rootCause.getMessage());
            } else {
                throw(e);
            }
        }
    }

    public DisplayContentDto setTemplateImage(String templateUuid, String displayContentDtoJson, MultipartFile image) {
        try {
            Template template = templateRepository.findByUuid(templateUuid);

            if (template == null) {
                throw new IllegalArgumentException("Template not found!");
            }

            DisplayContentDto displayContentDto = new ObjectMapper().readValue(displayContentDtoJson, DisplayContentDto.class);
            DisplayContent displayContent = modelMapper.map(displayContentDto, DisplayContent.class);

            boolean templateContentExists = template.getDisplayContent() != null;

            if (!templateContentExists) {
                template.setDisplayContent(new DisplayContent());
                template.getDisplayContent().setTemplate(template);
            }
            template.getDisplayContent().setImageFields(displayContent.getImageFields());
            template.getDisplayContent().setPadding(displayContent.getPadding());

            if (image != null) {
                InputStream in = new ByteArrayInputStream(image.getBytes());
                BufferedImage bImageFromConvert = ImageIO.read(in);
                String fileKey = template.getDisplayContent().getUuid();
                fileImportStorageS3.upload(imageUtil.convertToMonochrome(bImageFromConvert), fileKey);
            }

            Template savedTemplate = templateRepository.saveAndFlush(template);

            if (templateContentExists) {
                return null;
            }
            return modelMapper.map(savedTemplate.getDisplayContent(), DisplayContentDto.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid image or display content JSON.", e);
        }
    }

    public void deleteTemplate(String uuid) {
        Template template = templateRepository.findByUuid(uuid);
        if (template == null) {
            throw new IllegalArgumentException("Template not found!");
        }
        templateRepository.delete(template);
    }

    public byte[] getTemplateImage(String uuid, boolean withTextFields) {
        try {
            Template template = templateRepository.findByUuid(uuid);
            if (template == null) {
                throw new IllegalArgumentException("Template not found!");
            }
            if (template.getDisplayContent() == null) {
                throw new IllegalArgumentException("Template has no image!");
            }

            byte[] image = fileImportStorageS3.download(template.getDisplayContent().getUuid());
            InputStream is = new ByteArrayInputStream(image);
            BufferedImage bImage = ImageIO.read(is);

            if (withTextFields) {
                int roomAmount = template.getMaxRooms();
                int padding = template.getDisplayContent().getPadding();
                int roomSectionHeight = (template.getResolution().getHeight() - (padding * 2)) / roomAmount;

                for (int roomIndex = 0; roomIndex <= roomAmount; roomIndex++) {
                    imageUtil.drawImageTextFields(
                        bImage, 
                        template.getDisplayContent().getImageFields(), 
                        null, 
                        roomIndex, 
                        roomSectionHeight, 
                        padding
                    );
                }
            }
            return imageUtil.convertToByteArray(bImage, false, null);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing image", e);
        }
    }
}
