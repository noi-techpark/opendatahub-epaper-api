// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.components;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.noi.edisplay.model.Resolution;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.ResolutionRepository;
import it.noi.edisplay.repositories.TemplateRepository;

@Component
public class DefaultDataLoader {

    public static final String EVENT_TEMPLATE_NAME = "NOI Template";
    private Logger logger = LoggerFactory.getLogger(DefaultDataLoader.class);
    @Value("${cron.enabled}")
    private Boolean enabled;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private ResolutionRepository resolutionRepository;

    @PostConstruct
    public void onStartUp() throws IOException {

        if (templateRepository.findAll().size() == 0) {

            Template officeTemplate = new Template();
            Template meetingRoomTemplate = new Template();
            Template freeSoftwareLabTemplate = new Template();
            Template noiTemplate = new Template();
            
            Resolution resolution = new Resolution();
            resolution.setWidth(1440);
            resolution.setHeight(2560);
            resolution.setBitDepth(24);
            resolutionRepository.save(resolution);
            
            Resolution resolutionSmall = new Resolution();
            resolutionSmall.setWidth(1872);
            resolutionSmall.setHeight(1404);
            resolutionSmall.setBitDepth(24);
            resolutionRepository.save(resolutionSmall);
            
            Resolution resolutionBig = new Resolution();
            resolutionBig.setWidth(1440);
            resolutionBig.setHeight(5120);
            resolutionBig.setBitDepth(24);
            resolutionRepository.save(resolutionBig);

            officeTemplate.setName("Office");
            meetingRoomTemplate.setName("Meeting Room");
            freeSoftwareLabTemplate.setName("Free Software Lab");
            noiTemplate.setName(EVENT_TEMPLATE_NAME);
            officeTemplate.setResolution(resolution);
            meetingRoomTemplate.setResolution(resolution);
            freeSoftwareLabTemplate.setResolution(resolution);
            noiTemplate.setResolution(resolution);
            officeTemplate.setFooter(false);
            meetingRoomTemplate.setFooter(false);
            freeSoftwareLabTemplate.setFooter(false);
            noiTemplate.setFooter(false);
            officeTemplate.setHeader(false);
            meetingRoomTemplate.setHeader(false);
            freeSoftwareLabTemplate.setHeader(false);
            noiTemplate.setHeader(false);
            officeTemplate.setMultipleRoom(false);
            meetingRoomTemplate.setMultipleRoom(false);
            freeSoftwareLabTemplate.setMultipleRoom(false);
            noiTemplate.setMultipleRoom(false);

            templateRepository.save(officeTemplate);
            templateRepository.save(meetingRoomTemplate);
            templateRepository.save(noiTemplate);
            templateRepository.save(freeSoftwareLabTemplate);

        }

        if (resolutionRepository.findAll().size() == 0) {
            Resolution resolution = new Resolution();
            resolution.setWidth(1440);
            resolution.setHeight(2560);
            resolution.setBitDepth(24);
            resolutionRepository.save(resolution);
            Resolution resolutionSmall = new Resolution();
            resolutionSmall.setWidth(1872);
            resolutionSmall.setHeight(1404);
            resolutionSmall.setBitDepth(4);
            resolutionRepository.save(resolutionSmall);
            Resolution resolutionBig = new Resolution();
            resolutionSmall.setWidth(1440);
            resolutionSmall.setHeight(5120);
            resolutionSmall.setBitDepth(24);
            resolutionRepository.save(resolutionBig);
        }
    }
}
