package it.noi.edisplay.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import it.noi.edisplay.dto.EventDto;

import javax.persistence.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

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

//	@NotNull
    private String name;

//	@NotNull
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

    private String warningMessage;

//	@NotNull
    @ManyToOne
    private Resolution resolution;

    @ManyToOne
    private Template template;

    @ManyToOne
    private Location location;

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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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

        // Location
        if (this.getLocation() != null) {
            fieldValues.put(ImageFieldType.LOCATION_NAME, this.getLocation().getName());
        } else {
            fieldValues.put(ImageFieldType.LOCATION_NAME, "Location not specified");
        }

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
        }
        if (currentDisplayContent == null) {
            currentDisplayContent = display.getDisplayContent();
        }
        return currentDisplayContent;
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
}
