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

        // add default templates
        // addTemplate("Office");
        // addTemplate("Meeting Room");
        // addTemplate("Free Software Lab");
        // addTemplate(EVENT_TEMPLATE_NAME);

        // add default resolutions
        addResolution(1440, 2560, 24);
        addResolution(1872, 1404, 4);
        addResolution(1440, 5120, 24);
    }

    private void addResolution(int width, int height, int bitDepth) {
        if (resolutionRepository.findByWidthAndHeightAndBitDepth(width, height, bitDepth) == null) {
            Resolution resolution = new Resolution();
            resolution.setWidth(width);
            resolution.setHeight(height);
            resolution.setBitDepth(bitDepth);
            resolutionRepository.saveAndFlush(resolution);

            logger.info("New resolution with width: {} height: {} bidDepth: {} added.", width, height, bitDepth);
        }
    }

    private void addTemplate(String name) {
        if (templateRepository.findByName(name) == null) {
            Template template = new Template();
            template.setName(name);
            // template.setResolution(null);(name);

            templateRepository.saveAndFlush(template);

            logger.info("New template with name: {} added.", name);
        }
    }
}