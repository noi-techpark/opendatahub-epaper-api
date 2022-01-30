package it.noi.edisplay.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @OneToOne(mappedBy = "display", cascade = CascadeType.ALL)
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

    @PrePersist
    public void prePersist() {
        this.setUuid(UUID.randomUUID().toString());
        if (lastState == null)
            lastState = new Date();
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

    public boolean isIgnoringScheduledContent() {
        return ignoreScheduledContent;
    }

    public void setIgnoreScheduledContent(boolean ignoreScheduledContent) {
        this.ignoreScheduledContent = ignoreScheduledContent;
    }

    public Map<ImageFieldType, String> getTextFieldValues() {
        EnumMap<ImageFieldType, String> fieldValues = new EnumMap<>(ImageFieldType.class);

        // Location
        if (this.getLocation() != null) {
            fieldValues.put(ImageFieldType.LOCATION_NAME, this.getLocation().getName());
        } else {
            fieldValues.put(ImageFieldType.LOCATION_NAME, "Location not specified");
        }

        List<ScheduledContent> events = this.getScheduledContent();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        // Current Event
        Date currentDate = new Date();
        ScheduledContent currentEvent = events.stream()
                .filter(item -> item.getStartDate().before(currentDate) && item.getEndDate().after(currentDate))
                .findFirst().orElse(null);
        if (currentEvent != null) {
            fieldValues.put(ImageFieldType.EVENT_DESCRIPTION, currentEvent.getEventDescription());
            fieldValues.put(ImageFieldType.EVENT_START_DATE, f.format(currentEvent.getStartDate()));
            fieldValues.put(ImageFieldType.EVENT_END_DATE, f.format(currentEvent.getEndDate()));
        } else {
            fieldValues.put(ImageFieldType.EVENT_DESCRIPTION, "No current event");
            fieldValues.put(ImageFieldType.EVENT_START_DATE, "");
            fieldValues.put(ImageFieldType.EVENT_END_DATE, "");
        }

        // Upcoming event
        Collections.sort(events); // Sort events by start date
        if (!events.isEmpty()) {
            ScheduledContent upcomingEvent = events.get(0);
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_DESCRIPTION, upcomingEvent.getEventDescription());
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_START_DATE, f.format(upcomingEvent.getStartDate()));
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_END_DATE, f.format(upcomingEvent.getEndDate()));
        } else {
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_DESCRIPTION, "No upcoming events");
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_START_DATE, "");
            fieldValues.put(ImageFieldType.UPCOMING_EVENT_END_DATE, "");
        }
        return fieldValues;
    }

    public DisplayContent getCurrentDisplayContent() {
        DisplayContent currentDisplayContent = null;
        Display display = this;
        if (!display.isIgnoringScheduledContent()) {
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
}
