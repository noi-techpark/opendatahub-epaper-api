// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.noi.edisplay.components.NOIDataLoader;
import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.NOIPlaceData;
import it.noi.edisplay.dto.ScheduledContentDto;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.DisplayContent;
import it.noi.edisplay.model.ImageField;
import it.noi.edisplay.model.ScheduledContent;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.DisplayContentRepository;
import it.noi.edisplay.repositories.DisplayRepository;
import it.noi.edisplay.repositories.ScheduledContentRepository;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.storage.FileImportStorageS3;
import it.noi.edisplay.utils.ImageUtil;

@RestController
@RequestMapping("/ScheduledContent")
public class ScheduledContentController {
    @Autowired
    ScheduledContentRepository scheduledContentRepository;

    @Autowired
    DisplayRepository displayRepository;

    @Autowired
    DisplayContentRepository displayContentRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private NOIDataLoader noiDataLoader;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private FileImportStorageS3 fileImportStorageS3;

    @Autowired
    private ImageUtil imageUtil;

    Logger logger = LoggerFactory.getLogger(ScheduledContentController.class);

    @RequestMapping(value = "/get/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<ScheduledContentDto> getScheduledContent(@PathVariable("uuid") String uuid) {
        ScheduledContent scheduledContent = scheduledContentRepository.findByUuid(uuid);
        if (scheduledContent == null) {
            logger.debug("Scheduled content with uuid: " + uuid + " not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.debug("Get scheduled content with uuid: " + uuid);
        return new ResponseEntity<>(modelMapper.map(scheduledContent, ScheduledContentDto.class), HttpStatus.OK);
    }

    @GetMapping(value = "/get-image/{uuid}")
    public ResponseEntity<byte[]> getScheduledImage(@PathVariable("uuid") String uuid, boolean withTextFields)
            throws IOException {
        ScheduledContent scheduledContent = scheduledContentRepository.findByUuid(uuid);
        if (scheduledContent == null) {
            logger.debug("Scheduled Content with uuid: " + uuid + " not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (scheduledContent.getDisplayContent() == null) {
            logger.debug("Scheduled Content with uuid: " + uuid + " has no image.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (scheduledContent.getEventId() != null) {

        }
        byte[] image = fileImportStorageS3.download(scheduledContent.getDisplayContent().getUuid());

        logger.debug("Get scheduled content image with uuid: " + uuid);
        return new ResponseEntity<>(image, HttpStatus.OK);

    }

    @SuppressWarnings("null")
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseEntity getAllScheduledContents(@RequestParam String displayUuid) throws IOException {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Displays with uuid: " + displayUuid + " not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        boolean update = false;
        List<ScheduledContentDto> dtoList = noiDataLoader.getAllDisplayEvents(display);
        System.out.println(dtoList.size());
        if (dtoList != null) {

            if (display.getTemplate().isMultipleRoom()) {
                // boolean update = false;
                for (ScheduledContentDto check : dtoList) {
                    if (check.getDisplayContent() == null) {
                        update = true;
                        break;
                    } else {
                        Template template = templateRepository.findByUuid(display.getTemplate().getUuid());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        String checkFormatted = sdf.format(check.getLastUpdate());
                        String displayFormatted = sdf.format(display.getLastUpdate());
                        String templateFormatted = sdf.format(template.getLastUpdate());

                        if (check != null && checkFormatted.compareTo(displayFormatted) < 0) {
                            update = true;
                            break;
                        }

                        if (check != null && checkFormatted.compareTo(templateFormatted) < 0) {
                            update = true;
                            break;
                        }

                    }
                }
                if (update) {
                    updateEventsDataMulti(dtoList, display);
                }
            } else {
                for (ScheduledContentDto content : dtoList) {
                    ScheduledContent scheduledContent = null;

                    if (content.getUuid() != null && scheduledContentRepository.findByUuid(content.getUuid()) != null) {
                        scheduledContent = scheduledContentRepository.findByUuid(content.getUuid());
                    }

                    if (content.getEventId() != null && scheduledContentRepository
                            .findByDisplayIdAndEventId(display.getId(), content.getEventId()) != null) {
                        scheduledContent = scheduledContentRepository.findByDisplayIdAndEventId(display.getId(),
                                content.getEventId());
                    }
                    if (scheduledContent == null) {
                        scheduledContent = modelMapper.map(content, ScheduledContent.class);
                    }

                    for (ScheduledContentDto check : dtoList) {
                        if (check.getDisplayContent() == null) {
                            update = true;
                        }
                    }
                    if (update) {
                        updateScheduleData(scheduledContent, content, display);
                    }

                    /*
                     * if (scheduledContent != null && scheduledContent.getDisplayContent() != null
                     * &&
                     * scheduledContent.getLastUpdate().before(display.getTemplate().getLastUpdate()
                     * )) { updateScheduleData(scheduledContent, content, display);
                     * 
                     * }
                     */

                }
            }

        } else {
            logger.debug("No scheduled content found");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        logger.debug("All scheduled content requested");
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity createScheduledContent(@RequestBody ScheduledContentDto scheduledContentDto) {
        ScheduledContent scheduledContent = modelMapper.map(scheduledContentDto, ScheduledContent.class);
        scheduledContent.setDisplay(displayRepository.findByUuid(scheduledContentDto.getDisplayUuid()));
        for (NOIPlaceData place : noiDataLoader.getNOIPlaces()) {
            if (place.getScode().equals(scheduledContentDto.getRoom())) {
                scheduledContent.setSpaceDesc(place.getName());
            }
        }
        scheduledContent.setLastUpdate(new Date());
        scheduledContent.setDisabled(false);
        scheduledContent.setStartDate(scheduledContentDto.getStartDate());
        scheduledContent.setEndDate(scheduledContentDto.getEndDate());
        scheduledContent.setEventDescription(scheduledContentDto.getEventDescription());
        scheduledContent.setOverride(scheduledContentDto.getOverride());
        scheduledContent.setInclude(scheduledContentDto.getInclude());
        scheduledContent.setRoom(scheduledContentDto.getRoom());
        scheduledContent = scheduledContentRepository.saveAndFlush(scheduledContent);
        logger.debug("Scheduled content with uuid:" + scheduledContent.getUuid() + " created.");
        return new ResponseEntity<>(modelMapper.map(scheduledContent, ScheduledContentDto.class), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/delete/{uuid}", method = RequestMethod.DELETE)
    public ResponseEntity deleteScheduledContent(@PathVariable("uuid") String uuid) throws IOException {
        ScheduledContent scheduledContent = scheduledContentRepository.findByUuid(uuid);
        if (scheduledContent == null) {
            logger.debug("Delete scheduled content with uuid:" + uuid + " failed.");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        scheduledContentRepository.delete(scheduledContent);
        if (scheduledContent.getDisplay().getTemplate().isMultipleRoom()) {
            updateEventsDataMulti(noiDataLoader.getAllDisplayEvents(scheduledContent.getDisplay()),
                    scheduledContent.getDisplay());
        }
        logger.debug("Deleted scheduled content with uuid:" + uuid);
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = "application/json")
    public ResponseEntity updateScheduledContent(@RequestBody ScheduledContentDto scheduledContentDto)
            throws IOException {
        ScheduledContent scheduledContent;
        ScheduledContent existingScheduledContent = null;
        Display display = displayRepository.findByUuid(scheduledContentDto.getDisplayUuid());

        if (scheduledContentDto.getUuid() != null) {
            existingScheduledContent = scheduledContentRepository.findByUuid(scheduledContentDto.getUuid());
        } else {
            existingScheduledContent = scheduledContentRepository.findByDisplayIdAndEventId(display.getId(),
                    scheduledContentDto.getEventId());
        }
        if (existingScheduledContent == null) {
            scheduledContent = modelMapper.map(scheduledContentDto, ScheduledContent.class);
        } else {
            existingScheduledContent.setDisabled(scheduledContentDto.getDisabled());
            existingScheduledContent.setStartDate(scheduledContentDto.getStartDate());
            existingScheduledContent.setEndDate(scheduledContentDto.getEndDate());
            existingScheduledContent.setLastUpdate(new Date());
            existingScheduledContent.setEventDescription(scheduledContentDto.getEventDescription());
            existingScheduledContent.setOverride(scheduledContentDto.getOverride());
            existingScheduledContent.setInclude(scheduledContentDto.getInclude());
            existingScheduledContent.setRoom(scheduledContentDto.getRoom());
            scheduledContent = existingScheduledContent;

        }

        scheduledContent.setDisplay(display);

        scheduledContent = scheduledContentRepository.saveAndFlush(scheduledContent);
        logger.debug("Updated scheduled content with uuid:" + scheduledContent.getUuid());
        if (existingScheduledContent == null) {
            return new ResponseEntity<>(modelMapper.map(scheduledContent, ScheduledContentDto.class),
                    HttpStatus.CREATED);
        } else {
            return new ResponseEntity(HttpStatus.ACCEPTED);
        }
    }

    @PostMapping(value = "/set-new-image/{scheduledContentUuid}", consumes = "multipart/form-data")
    public ResponseEntity<DisplayContentDto> setDisplayContent(
            @PathVariable("scheduledContentUuid") String scheduledContentUuid,
            @RequestParam("displayContentDtoJson") String displayContentDtoJson,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        ScheduledContent scheduledContent = scheduledContentRepository.findByUuid(scheduledContentUuid);
        if (scheduledContent == null) {
            logger.debug("Scheduled Content with uuid " + scheduledContentUuid + " was not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        DisplayContentDto displayContentDto = new ObjectMapper().readValue(displayContentDtoJson,
                DisplayContentDto.class);

        DisplayContent displayContent = modelMapper.map(displayContentDto, DisplayContent.class);

        boolean displayContentExists = scheduledContent.getDisplayContent() != null;
        if (!displayContentExists) {
            scheduledContent.setDisplayContent(new DisplayContent());
            scheduledContent.getDisplayContent().setScheduledContent(scheduledContent);
        }
        ScheduledContent savedScheduledContent = null;
        if (scheduledContent.getDisplay().getTemplate().isMultipleRoom()) {
            scheduledContent.getDisplayContent().setImageFields(displayContent.getImageFields());
            scheduledContent.getDisplayContent().setImageHash(null);

            savedScheduledContent = scheduledContentRepository.saveAndFlush(scheduledContent);
            updateEventsDataMulti(noiDataLoader.getAllDisplayEvents(scheduledContent.getDisplay()),
                    scheduledContent.getDisplay());
        } else {

            scheduledContent.getDisplayContent().setImageFields(displayContent.getImageFields());

            scheduledContent.getDisplayContent()
                    .setImageBase64(imageUtil.drawImageTextFields(displayContent.getImageFields(),
                            scheduledContent.getDisplay().getResolution().getWidth(),
                            scheduledContent.getDisplay().getResolution().getHeight(),
                            scheduledContent.getDisplay().getTemplate()));
            if (scheduledContent.getDisplayContent().getImageBase64() != null) {
                BufferedImage bImageFromConvert = null;
                byte[] imageBytes = Base64.getDecoder()
                        .decode(imageUtil.drawImageTextFields(displayContent.getImageFields(),
                                scheduledContent.getDisplay().getResolution().getWidth(),
                                scheduledContent.getDisplay().getResolution().getHeight(),
                                scheduledContent.getDisplay().getTemplate()));
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                bImageFromConvert = ImageIO.read(bis);
                String fileKey = scheduledContent.getDisplayContent().getUuid();
                fileImportStorageS3.upload(imageUtil.convertToMonochrome(bImageFromConvert), fileKey);
            }
            scheduledContent.getDisplayContent().setImageHash(null);
            scheduledContent.setLastUpdate(new Date());
            savedScheduledContent = scheduledContentRepository.saveAndFlush(scheduledContent);

        }
        if (displayContentExists) {
            logger.debug("Updated image for Scheduled Content uuid:" + scheduledContentUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        logger.debug("Created image for Scheduled Content uuid:" + scheduledContentUuid);
        return new ResponseEntity<>(modelMapper.map(savedScheduledContent.getDisplayContent(), DisplayContentDto.class),
                HttpStatus.CREATED);
    }

    private void updateScheduleData(ScheduledContent scheduledContent, ScheduledContentDto event, Display display)
            throws IOException {
        if (scheduledContent != null) {
            scheduledContent.setDisplay(displayRepository.findByUuid(event.getDisplayUuid()));
            scheduledContent = scheduledContentRepository.saveAndFlush(scheduledContent);

            if (scheduledContent.getDisplayContent() == null) {
                scheduledContent = copySchedule(scheduledContent, event);
            }

            List<ImageField> imageFields = new ArrayList<>();
            for (ImageField imageField : display.getTemplate().getDisplayContent().getImageFields()) {
                SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy | HH:mm");
                f.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));

                if (imageField.getFieldType().toString().equals("OTHER")) {
                    imageFields.add(copy(imageField, imageField.getCustomText()));
                }
                if (event.getOriginalEventDescription() != null
                        && imageField.getFieldType().toString().equals("EVENT_DESCRIPTION")) {
                    imageFields.add(copy(imageField, event.getEventDescription().toString()));
                }
                if (event.getCompanyName() != null && imageField.getFieldType().toString().equals("EVENT_ORGANIZER")) {
                    imageFields.add(copy(imageField, event.getCompanyName().toString()));
                }
                if (scheduledContent.getStartDate() != null
                        && imageField.getFieldType().toString().equals("EVENT_START_DATE")) {
                    if (event.getStartDate() != null) {
                        imageFields.add(copy(imageField, f.format(event.getStartDate())));
                    }
                }
                if (scheduledContent.getEndDate() != null
                        && imageField.getFieldType().toString().equals("EVENT_END_DATE")) {
                    if (event.getStartDate() != null) {
                        imageFields.add(copy(imageField, f.format(event.getEndDate())));
                    }
                }
                if (event.getSpaceDesc() != null && imageField.getFieldType().toString().equals("LOCATION_NAME")) {
                    imageFields.add(copy(imageField, event.getSpaceDesc().toString()));

                }

            }
            boolean displayContentExists = scheduledContent.getDisplayContent() != null;
            if (!displayContentExists) {
                scheduledContent.setDisplayContent(new DisplayContent());
                scheduledContent.getDisplayContent().setScheduledContent(scheduledContent);
            }
            scheduledContent.getDisplayContent().setImageFields(imageFields);
            scheduledContent.getDisplayContent().setImageHash(null);
            scheduledContent.getDisplayContent()
                    .setImageBase64(imageUtil.drawImageTextFields(imageFields, display.getResolution().getWidth(),
                            display.getResolution().getHeight(), scheduledContent.getDisplay().getTemplate()));
            scheduledContentRepository.saveAndFlush(scheduledContent);
            BufferedImage bImageFromConvert = null;
            byte[] imageBytes = Base64.getDecoder()
                    .decode(imageUtil.drawImageTextFields(imageFields, display.getResolution().getWidth(),
                            display.getResolution().getHeight(), scheduledContent.getDisplay().getTemplate()));
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            bImageFromConvert = ImageIO.read(bis);
            String fileKey = scheduledContent.getDisplayContent().getUuid();
            fileImportStorageS3.upload(imageUtil.convertToMonochrome(bImageFromConvert), fileKey);
        }
    }

    private void updateEventsDataMulti(List<ScheduledContentDto> dtoList, Display display) throws IOException {

        for (ScheduledContentDto content : dtoList) {
            ScheduledContent scheduledContent11 = null;

            if (content.getUuid() != null && scheduledContentRepository.findByUuid(content.getUuid()) != null) {
                scheduledContent11 = scheduledContentRepository.findByUuid(content.getUuid());

            }

            if (content.getEventId() != null && scheduledContentRepository.findByDisplayIdAndEventId(display.getId(),
                    content.getEventId()) != null) {
                scheduledContent11 = scheduledContentRepository.findByDisplayIdAndEventId(display.getId(),
                        content.getEventId());
            }

            if (scheduledContent11 == null) {
                scheduledContent11 = modelMapper.map(content, ScheduledContent.class);
            }

            if (scheduledContent11 != null) {
                // updateMultiScreenEvents(scheduledContent, content, dtoList, display);
                ScheduledContent scheduledContentoriginal = scheduledContent11;
                ScheduledContentDto event = content;

                if (scheduledContentoriginal != null) {
                    scheduledContentoriginal.setDisplay(displayRepository.findByUuid(event.getDisplayUuid()));
                    scheduledContentoriginal = scheduledContentRepository.saveAndFlush(scheduledContentoriginal);

                    if (scheduledContentoriginal.getDisplayContent() == null) {
                        scheduledContentoriginal = copySchedule(scheduledContentoriginal, event);
                    }
                    List<ImageField> imagesFields = new ArrayList<>();
                    List<ImageField> someimagesFields = new ArrayList<>();

                    if (!display.getIgnoreScheduledContent() && display.getScheduledContent() != null) {
                        Date currentDate = scheduledContentoriginal.getStartDate();
                        // all events after the original
                        List<ScheduledContentDto> futureEvents = dtoList.stream()
                                .filter(item -> item.getStartDate().after(currentDate)
                                        || item.getStartDate().equals(currentDate))
                                .sorted(Comparator.comparing(ScheduledContentDto::getStartDate))
                                .collect(Collectors.toList());
                        // all events after the original
                        List<ScheduledContent> finalList = new ArrayList<>();
                        for (ScheduledContentDto list : futureEvents) {
                            if (scheduledContentRepository.findByUuid(list.getUuid()) != null) {
                                finalList.add(scheduledContentRepository.findByUuid(list.getUuid()));
                            } else {
                                finalList.add(modelMapper.map(list, ScheduledContent.class));
                            }
                        }
                        // events in future but will certain room
                        List<ScheduledContent> finalList2 = new ArrayList<>();
                        // order based to room
                        List<ScheduledContent> finalList3 = new ArrayList<>();
                        Set<String> addedRooms = new HashSet<>();

                        finalList2.add(scheduledContentoriginal);
                        addedRooms.add(scheduledContentoriginal.getRoom());

                        for (String room : display.getRoomCodes()) {
                            for (ScheduledContent scheduledContent : finalList) {
                                if (room.equals(scheduledContent.getRoom()) && !addedRooms.contains(room)
                                        && !finalList2.contains(scheduledContent)) {
                                    finalList2.add(scheduledContent);
                                    addedRooms.add(room);
                                    break;
                                }
                            }
                        }

                        for (String room : display.getRoomCodes()) {
                            for (ScheduledContent scheduledContent : finalList2) {
                                if (room.equals(scheduledContent.getRoom())) {
                                    finalList3.add(scheduledContent);
                                    break;
                                }
                            }
                        }
                        // save contant data in first position
                        List<ImageField> scheduledImagesFields = new ArrayList<>();
                        // save just content data
                        List<ImageField> scheduledImagesFieldsContent = new ArrayList<>();
                        int index = 1;
                        for (ImageField fields : display.getTemplate().getDisplayContent().getImageFields()) {
                            if (!fields.isRepeat() && !fields.isRepeated()) {
                                imagesFields.add(copy(fields, fields.getCustomText()));

                            }
                        }

                        for (ScheduledContent scheduledContent : finalList3) {

                            int start = display.getTemplate().getRoomData()[1]
                                    + (index - 1) * display.getTemplate().getRoomData()[2];
                            int end = start + display.getTemplate().getRoomData()[2];

                            for (ImageField fields : display.getTemplate().getDisplayContent().getImageFields()) {
                                if (fields.getyPos() >= start && fields.getyPos() <= end
                                        && (fields.isRepeat() || fields.isRepeated())) {
                                    someimagesFields.add(copy(fields, fields.getCustomText()));
                                }
                                if (index == 1 && fields.getyPos() >= start && fields.getyPos() <= end
                                        && (fields.isRepeat() || fields.isRepeated())) {
                                    scheduledImagesFields.add(copy(fields, fields.getCustomText()));
                                }

                            }
                            SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy | HH:mm");
                            f.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
                            for (ImageField imageField : someimagesFields) {
                                if (scheduledContent != null && scheduledContent.getDisplayContent() != null
                                        && scheduledContent.getDisplayContent().getImageFields() != null) {
                                    for (ImageField img : scheduledContent.getDisplayContent().getImageFields()) {

                                        if (img.getFieldType().toString()
                                                .equals(imageField.getFieldType().toString())) {

                                            imagesFields.add(copy(imageField, img.getCustomText()));
                                            if (similar(scheduledContent, scheduledContentoriginal)) {
                                                scheduledImagesFieldsContent.add(copy2(scheduledImagesFields,
                                                        imageField.getFieldType().toString(), img.getCustomText()));
                                            }

                                        }
                                    }

                                } else {
                                    if (scheduledContent.getEventDescription() != null
                                            && imageField.getFieldType().toString().equals("EVENT_DESCRIPTION")) {
                                        imagesFields.add(
                                                copy(imageField, scheduledContent.getEventDescription().toString()));
                                        if (similar(scheduledContent, scheduledContentoriginal)) {
                                            scheduledImagesFieldsContent.add(
                                                    copy2(scheduledImagesFields, imageField.getFieldType().toString(),
                                                            scheduledContent.getEventDescription().toString()));
                                        }
                                    }
                                    if (scheduledContent.getCompanyName() != null
                                            && imageField.getFieldType().toString().equals("EVENT_ORGANIZER")) {
                                        imagesFields
                                                .add(copy(imageField, scheduledContent.getCompanyName().toString()));
                                        if (similar(scheduledContent, scheduledContentoriginal)) {
                                            scheduledImagesFieldsContent.add(
                                                    copy2(scheduledImagesFields, imageField.getFieldType().toString(),
                                                            scheduledContent.getCompanyName().toString()));
                                        }
                                    }
                                    if (scheduledContent.getStartDate() != null
                                            && imageField.getFieldType().toString().equals("EVENT_START_DATE")) {
                                        imagesFields.add(copy(imageField, f.format(scheduledContent.getStartDate())));
                                        if (similar(scheduledContentoriginal, scheduledContent)) {
                                            scheduledImagesFieldsContent.add(
                                                    copy2(scheduledImagesFields, imageField.getFieldType().toString(),
                                                            f.format(scheduledContent.getStartDate())));
                                        }
                                    }
                                    if (scheduledContent.getEndDate() != null
                                            && imageField.getFieldType().toString().equals("EVENT_END_DATE")) {
                                        imagesFields.add(copy(imageField, f.format(scheduledContent.getEndDate())));

                                        if (similar(scheduledContentoriginal, scheduledContent)) {
                                            scheduledImagesFieldsContent.add(
                                                    copy2(scheduledImagesFields, imageField.getFieldType().toString(),
                                                            f.format(scheduledContent.getEndDate())));
                                        }
                                    }
                                    if (scheduledContent.getSpaceDesc() != null
                                            && imageField.getFieldType().toString().equals("LOCATION_NAME")) {
                                        imagesFields.add(copy(imageField, scheduledContent.getSpaceDesc().toString()));
                                        if (similar(scheduledContentoriginal, scheduledContent)) {
                                            scheduledImagesFieldsContent.add(
                                                    copy2(scheduledImagesFields, imageField.getFieldType().toString(),
                                                            scheduledContent.getSpaceDesc().toString()));
                                        }
                                    }
                                }
                            }

                            if (scheduledContent != null && scheduledContent.getDisplayContent() != null
                                    && scheduledContent.getDisplayContent().getImageFields() != null) {

                                for (ImageField img : scheduledContent.getDisplayContent().getImageFields()) {
                                    if (!type(someimagesFields, img)) {
                                        while (!(img.getyPos() >= start && img.getyPos() <= end)) {
                                            img.setyPos(img.getyPos() - display.getTemplate().getRoomData()[2]);
                                        }
                                        imagesFields.add(img);
                                        scheduledImagesFieldsContent.add(copy(img, img.getCustomText()));

                                    }
                                }

                            }

                            someimagesFields.clear();
                            index++;

                        }
                        boolean displayContentExists = scheduledContentoriginal.getDisplayContent() != null;
                        if (!displayContentExists) {
                            scheduledContentoriginal.setDisplayContent(new DisplayContent());
                            scheduledContentoriginal.getDisplayContent().setScheduledContent(scheduledContentoriginal);
                        }
                        scheduledContentoriginal.getDisplayContent().setImageFields(scheduledImagesFieldsContent);

                        scheduledContentoriginal.getDisplayContent().setImageHash(null);

                        scheduledContentoriginal.getDisplayContent()
                                .setImageBase64(imageUtil.drawImageTextFields(imagesFields,
                                        display.getResolution().getWidth(), display.getResolution().getHeight(),
                                        scheduledContentoriginal.getDisplay().getTemplate()));
                        scheduledContentoriginal.setLastUpdate(new Date());
                        scheduledContentRepository.saveAndFlush(scheduledContentoriginal);
                        BufferedImage bImageFromConvert = null;
                        byte[] imageBytes = Base64.getDecoder()
                                .decode(imageUtil.drawImageTextFields(imagesFields, display.getResolution().getWidth(),
                                        display.getResolution().getHeight(),
                                        scheduledContentoriginal.getDisplay().getTemplate()));
                        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                        bImageFromConvert = ImageIO.read(bis);
                        String fileKey = scheduledContentoriginal.getDisplayContent().getUuid();
                        fileImportStorageS3.upload(imageUtil.convertToMonochrome(bImageFromConvert), fileKey);

                    }

                }

            }
        }

    }

    private boolean type(List<ImageField> imageFields, ImageField imageField) {
        for (ImageField img : imageFields) {
            if (img.getFieldType().toString().equals(imageField.getFieldType().toString())) {
                return true;
            }
        }
        return false;
    }

    private ImageField copy2(List<ImageField> imageFields, String type, String text) {
        ImageField imageFieldNew = new ImageField();
        for (ImageField imageField : imageFields) {
            if (imageField.getFieldType().toString().equals(type)) {
                imageFieldNew.setBold(imageField.isBold());
                imageFieldNew.setBorder(imageField.isBorder());
                imageFieldNew.setInvert(imageField.isInvert());
                imageFieldNew.setCreated(new Date());
                imageFieldNew.setFieldType(imageField.getFieldType());
                imageFieldNew.setCustomText(text);
                imageFieldNew.setFieldType(imageField.getFieldType());
                imageFieldNew.setFontSize(imageField.getFontSize());
                imageFieldNew.setHeight(imageField.getHeight());
                imageFieldNew.setImage(imageField.getImage());
                imageFieldNew.setItalic(imageField.isItalic());
                imageFieldNew.setLastUpdate(new Date());
                imageFieldNew.setRepeat(imageField.isRepeat());
                imageFieldNew.setRepeated(imageField.isRepeated());
                imageFieldNew.setWidth(imageField.getWidth());
                imageFieldNew.setxPos(imageField.getxPos());
                imageFieldNew.setyPos(imageField.getyPos());
                return imageFieldNew;
            }
        }
        return null;
    }

    private boolean similar(ScheduledContent sch1, ScheduledContent sc2) {
        return sch1.equals(sc2);
    }

    private ImageField copy(ImageField imageField, String text) {
        ImageField imageFieldNew = new ImageField();
        imageFieldNew.setBold(imageField.isBold());
        imageFieldNew.setBorder(imageField.isBorder());
        imageFieldNew.setCreated(new Date());
        imageFieldNew.setInvert(imageField.isInvert());
        imageFieldNew.setFieldType(imageField.getFieldType());
        imageFieldNew.setCustomText(text);
        imageFieldNew.setFieldType(imageField.getFieldType());
        imageFieldNew.setFontSize(imageField.getFontSize());
        imageFieldNew.setHeight(imageField.getHeight());
        imageFieldNew.setImage(imageField.getImage());
        imageFieldNew.setItalic(imageField.isItalic());
        imageFieldNew.setLastUpdate(new Date());
        imageFieldNew.setRepeat(imageField.isRepeat());
        imageFieldNew.setRepeated(imageField.isRepeated());
        imageFieldNew.setWidth(imageField.getWidth());
        imageFieldNew.setxPos(imageField.getxPos());
        imageFieldNew.setyPos(imageField.getyPos());
        return imageFieldNew;
    }

    private ScheduledContent copySchedule(ScheduledContent scheduledContent, ScheduledContentDto event) {
        scheduledContent.setCreated(event.getCreated() != null ? event.getCreated() : new Date());
        scheduledContent.setCreated(event.getCreated() != null ? event.getCreated() : new Date());
        scheduledContent.setLastUpdate(new Date());
        scheduledContent.setDisabled(event.getDisabled() != null ? event.getDisabled() : false);
        scheduledContent.setInclude(false);
        scheduledContent.setOverride(false);
        return scheduledContent;
    }

}
