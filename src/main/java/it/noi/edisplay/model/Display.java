// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.utils.ImageUtil;

/**
 * Entity class for Displays that contains all needed information for an
 * E-Display
 *
 * @Author Simon Dalvai
 */
@Entity
@Table(name = "displays")
public class Display {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // @NotNull
    private String name;

    // @NotNull
    private String uuid;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    // Saves timestamp when logical display gets updated
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    // Saves timestamp when real physical display gets updated
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastRealDisplayUpdate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastState;

    private String errorMessage;

    private String imageBase64;

    private String warningMessage;

    private String imageHash;

    // @NotNull
    @ManyToOne
    private Resolution resolution;

    @ManyToOne
    private Template template;

    @Type(type = "string-array")
    @Column(name = "room_codes", columnDefinition = "text[]")
    private String[] roomCodes;

    @OneToOne(mappedBy = "display", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private DisplayContent displayContent;

    @OneToMany(mappedBy = "display", fetch = FetchType.LAZY)
    private List<ScheduledContent> scheduledContent;

    private int batteryPercentage;

    private boolean ignoreScheduledContent;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Date getLastState() {
        return lastState;
    }

    public void setLastState(Date lastState) {
        this.lastState = lastState;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(int batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public Date getLastRealDisplayUpdate() {
        return lastRealDisplayUpdate;
    }

    public void setLastRealDisplayUpdate(Date lastRealDisplayUpdate) {
        this.lastRealDisplayUpdate = lastRealDisplayUpdate;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public DisplayContent getDisplayContent() {
        return displayContent;
    }

    public void setDisplayContent(DisplayContent displayContent) {
        this.displayContent = displayContent;
    }

    public List<ScheduledContent> getScheduledContent() {
        return scheduledContent;
    }

    public void setScheduledContent(List<ScheduledContent> scheduledContent) {
        this.scheduledContent = scheduledContent;
    }

    public Map<ImageFieldType, String> getTextFieldValues(List<EventDto> events, int eventAdvance) {
        EnumMap<ImageFieldType, String> fieldValues = new EnumMap<>(ImageFieldType.class);
        // transform minutes in milliseconds
        eventAdvance *= 60000;
        // TODO replace with room name
        // Location
        // if (this.getLocation() != null) {
        // fieldValues.put(ImageFieldType.LOCATION_NAME, this.getLocation().getName());
        // } else {
        // fieldValues.put(ImageFieldType.LOCATION_NAME, "Location not specified");
        // }

        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy | HH:mm");
        f.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));

        // Current Event
        Long currentTime = System.currentTimeMillis();
        Long currentTimePlusAdvance = currentTime + eventAdvance;

        EventDto currentEvent = events.stream().filter(
                item -> item.getRoomStartDateUTC() < currentTimePlusAdvance && item.getRoomEndDateUTC() > currentTime)
                .findFirst().orElse(null);
        if (currentEvent != null) {
            fieldValues.put(ImageFieldType.EVENT_DESCRIPTION, formEventDescription(currentEvent));
            fieldValues.put(ImageFieldType.EVENT_ORGANIZER, currentEvent.getCompanyName());
            fieldValues.put(ImageFieldType.EVENT_START_DATE,
                    f.format(new Timestamp((currentEvent.getRoomStartDateUTC()))));
            fieldValues.put(ImageFieldType.EVENT_END_DATE, f.format(new Timestamp((currentEvent.getRoomEndDateUTC()))));
        } else {
            fieldValues.put(ImageFieldType.EVENT_DESCRIPTION, "Welcome to NOI Techpark");
            fieldValues.put(ImageFieldType.EVENT_ORGANIZER, "");
            fieldValues.put(ImageFieldType.EVENT_START_DATE, "");
            fieldValues.put(ImageFieldType.EVENT_END_DATE, "");
        }

        // Upcoming event
        List<EventDto> upcomingEvents = events.stream()
                .filter(item -> item.getRoomStartDateUTC() > currentTimePlusAdvance).collect(Collectors.toList());
        if (!upcomingEvents.isEmpty()) {
            Collections.sort(upcomingEvents); // Sort events by start date
            EventDto upcomingEvent = upcomingEvents.get(0);
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_DESCRIPTION, formEventDescription(upcomingEvent));
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_ORGANIZER, upcomingEvent.getCompanyName());
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_START_DATE,
                    f.format(new Timestamp((upcomingEvent.getRoomStartDateUTC()))));
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_END_DATE,
                    f.format(new Timestamp((upcomingEvent.getRoomEndDateUTC()))));
        } else {
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_DESCRIPTION, "No upcoming events");
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_ORGANIZER, "");
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_START_DATE, "");
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_END_DATE, "");
        }
        return fieldValues;
    }

    public DisplayContent getCurrentContent() {

        DisplayContent currentDisplayContent = null;
        Display display = this;
        if (!display.getIgnoreScheduledContent() && display.getScheduledContent() != null) {
            // Current Event
            Date currentDate = new Date();
            ScheduledContent currentEvent = display.getScheduledContent().stream()
                    .filter(item -> item.getStartDate().before(currentDate) && item.getEndDate().after(currentDate))
                    .findFirst().orElse(null);
            if (currentEvent != null && !Boolean.TRUE.equals(currentEvent.getDisabled())
                    && currentEvent.getDisplayContent() != null) {
                currentDisplayContent = currentEvent.getDisplayContent();
            }
            ScheduledContent overrideEvent = display
                    .getScheduledContent().stream().filter(item -> item.getOverride()
                            && item.getStartDate().before(currentDate) && item.getEndDate().after(currentDate))
                    .findFirst().orElse(null);
            if (overrideEvent != null && !Boolean.TRUE.equals(overrideEvent.getDisabled())
                    && overrideEvent.getDisplayContent() != null) {
                return overrideEvent.getDisplayContent();
            }
        }

        if (currentDisplayContent == null) {
            currentDisplayContent = display.getDisplayContent();
        }
        return currentDisplayContent;

    }

    @SuppressWarnings("null")
    public void getCurrentContentMultiRooms() {
        Display display = this;
        List<DisplayContent> currentDisplayContents = new ArrayList<>();
        ScheduledContent currentEvent = null;
        List<ScheduledContent> currents = new ArrayList<>();
        List<ImageField> imagesFields = new ArrayList<>();
        List<ImageField> someimagesFields = new ArrayList<>();
        if (!display.getIgnoreScheduledContent() && display.getScheduledContent() != null) {
            Date currentDate = new Date();

            // Find all current events
            List<ScheduledContent> currentEvents = display.getScheduledContent().stream()
                    .filter(item -> item.getStartDate().before(currentDate) && item.getEndDate().after(currentDate))
                    .collect(Collectors.toList());
            // Add display content from current events
            for (ScheduledContent event : currentEvents) {
                if (!Boolean.TRUE.equals(event.getDisabled()) && event.getDisplayContent() != null) {
                    currentDisplayContents.add(event.getDisplayContent());
                    currents.add(event);
                }
            }
            Optional<ScheduledContent> earliestEvent = currentEvents.stream()
                    .min(Comparator.comparing(ScheduledContent::getEndDate));

            // Get the earliest event if present
            if (earliestEvent.isPresent()) {
                currentEvent = earliestEvent.get();
                // Use the earliest event as needed
            }

            for (ImageField field : display.getTemplate().getDisplayContent().getImageFields()) {
                if (!field.isRepeat() && !field.isRepeated()) {
                    imagesFields.add(field);
                }
            }

            int index = 1;

            for (ImageField fields : display.getTemplate().getDisplayContent().getImageFields()) {
                if (!fields.isRepeat() && !fields.isRepeated()) {
                    imagesFields.add(fields);
                }
            }
            for (String room : display.getRoomCodes()) {
                ScheduledContent scheduledContent = null;
                Iterator<ScheduledContent> iterator = currents.iterator();
                while (iterator.hasNext()) {
                    ScheduledContent scheduledd1 = iterator.next();
                    if (room.equals(scheduledd1.getRoom())) {
                        scheduledContent = scheduledd1;
                        iterator.remove(); // Use iterator to safely remove the item
                        break; // Break the loop as we found the item
                    }
                }

                if (scheduledContent != null) {
                    int start = display.getTemplate().getRoomData()[1]
                            + (index - 1) * display.getTemplate().getRoomData()[2];
                    int end = start + display.getTemplate().getRoomData()[2];

                    for (ImageField fields : display.getTemplate().getDisplayContent().getImageFields()) {
                        if (fields.getyPos() >= start && fields.getyPos() <= end
                                && (fields.isRepeat() || fields.isRepeated())) {
                            someimagesFields.add(fields);
                        }

                    }

                    for (ImageField fields : scheduledContent.getDisplayContent().getImageFields()) {
                        for (ImageField field : someimagesFields) {

                            if (field.getFieldType().toString() == fields.getFieldType().toString()) {
                                field.setCustomText(fields.getCustomText());
                                imagesFields.add(field);
                            }
                        }
                    }
                    someimagesFields.clear();

                    for (ImageField fields : scheduledContent.getDisplayContent().getImageFields()) {
                        if (!fields.isRepeat() && !fields.isRepeated()) {
                            // check
                            while (!(fields.getyPos() >= start && fields.getyPos() <= end)) {
                                fields.setyPos(fields.getyPos() - display.getTemplate().getRoomData()[2]);
                            }
                            imagesFields.add(fields);
                        }

                    }
                    index++;
                }

            }
            ImageUtil imageUtil = new ImageUtil();
            imageUtil.drawImageTextFields(null, imagesFields, display.getResolution().getWidth(),
                    display.getResolution().getHeight());

        }

    }

    public String getCurrentContentMultiRoomsImage() {
        Display display = this;
        List<DisplayContent> currentDisplayContents = new ArrayList<>();
        List<ScheduledContent> currents = new ArrayList<>();
        List<ImageField> imagesFields = new ArrayList<>();
        List<ImageField> someimagesFields = new ArrayList<>();
        if (!display.getIgnoreScheduledContent() && display.getScheduledContent() != null) {
            Date currentDate = new Date();

            // Find all current events
            List<ScheduledContent> currentEvents = display.getScheduledContent().stream()
                    .filter(item -> item.getStartDate().before(currentDate) && item.getEndDate().after(currentDate))
                    .collect(Collectors.toList());

            // Add display content from current events
            for (ScheduledContent event : currentEvents) {
                if (!Boolean.TRUE.equals(event.getDisabled()) && event.getDisplayContent() != null) {
                    currentDisplayContents.add(event.getDisplayContent());
                    currents.add(event);
                }
            }
            for (ImageField field : display.getTemplate().getDisplayContent().getImageFields()) {
                if (!field.isRepeat() && !field.isRepeated()) {
                    imagesFields.add(field);
                }
            }

            int index = 1;

            for (ImageField fields : display.getTemplate().getDisplayContent().getImageFields()) {
                if (!fields.isRepeat() && !fields.isRepeated()) {
                    imagesFields.add(fields);
                }
            }
            for (String room : display.getRoomCodes()) {
                ScheduledContent scheduledContent = null;
                Iterator<ScheduledContent> iterator = currents.iterator();
                while (iterator.hasNext()) {
                    ScheduledContent scheduledd1 = iterator.next();
                    if (room.equals(scheduledd1.getRoom())) {
                        scheduledContent = scheduledd1;
                        iterator.remove(); // Use iterator to safely remove the item
                        break; // Break the loop as we found the item
                    }
                }

                if (scheduledContent != null) {
                    int start = display.getTemplate().getRoomData()[1]
                            + (index - 1) * display.getTemplate().getRoomData()[2];
                    int end = start + display.getTemplate().getRoomData()[2];

                    for (ImageField fields : display.getTemplate().getDisplayContent().getImageFields()) {
                        if (fields.getyPos() >= start && fields.getyPos() <= end
                                && (fields.isRepeat() || fields.isRepeated())) {
                            someimagesFields.add(fields);
                        }

                    }

                    for (ImageField fields : scheduledContent.getDisplayContent().getImageFields()) {
                        for (ImageField field : someimagesFields) {

                            if (field.getFieldType().toString() == fields.getFieldType().toString()) {
                                field.setCustomText(fields.getCustomText());
                                imagesFields.add(field);
                            }
                        }
                    }
                    someimagesFields.clear();

                    for (ImageField fields : scheduledContent.getDisplayContent().getImageFields()) {
                        if (!fields.isRepeat() && !fields.isRepeated()) {
                            // check
                            while (!(fields.getyPos() >= start && fields.getyPos() <= end)) {
                                fields.setyPos(fields.getyPos() - display.getTemplate().getRoomData()[2]);
                            }
                            imagesFields.add(fields);
                        }

                    }
                    index++;
                }

            }

        }
        ImageUtil imageUtil = new ImageUtil();
        return imageUtil.drawImageTextFields(null, imagesFields, display.getResolution().getWidth(),
                display.getResolution().getHeight());

    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public boolean getIgnoreScheduledContent() {
        return ignoreScheduledContent;
    }

    public void setIgnoreScheduledContent(boolean ignoreScheduledContent) {
        this.ignoreScheduledContent = ignoreScheduledContent;
    }

    private String formEventDescription(EventDto eventDto) {
        String descriptionEN = eventDto.getEventDescriptionEN().trim().toLowerCase();
        String descriptionDE = eventDto.getEventDescriptionDE().trim().toLowerCase();
        String descriptionIT = eventDto.getEventDescriptionIT().trim().toLowerCase();

        if (descriptionEN.equals(descriptionDE) && descriptionDE.equals(descriptionIT)) {
            // All descriptions duplicate, return one
            return eventDto.getEventDescriptionEN();
        } else if (descriptionEN.equals(descriptionDE)) {
            // EN and DE are duplicates, return EN/DE + IT
            return eventDto.getEventDescriptionEN() + "\n" + eventDto.getEventDescriptionIT();
        } else if (descriptionEN.equals(descriptionIT)) {
            // EN and IT are duplicates, return DE + EN/IT
            return eventDto.getEventDescriptionEN() + "\n" + eventDto.getEventDescriptionDE();
        } else if (descriptionIT.equals(descriptionDE)) {
            // IT and DE are duplicates, return IT/DE + EN
            return eventDto.getEventDescriptionIT() + "\n" + eventDto.getEventDescriptionEN();
        } else {
            // Descriptions in all languages are unique, return all
            return eventDto.getEventDescriptionDE() + "\n" + eventDto.getEventDescriptionEN() + "\n"
                    + eventDto.getEventDescriptionIT();
        }
    }

    public String[] getRoomCodes() {
        return roomCodes;
    }

    public void setRoomCodes(String[] roomCodes) {
        this.roomCodes = roomCodes;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getImageHash() {
        return imageHash;
    }

    public void setImageHash(String imageHash) {
        this.imageHash = imageHash;
    }

}
