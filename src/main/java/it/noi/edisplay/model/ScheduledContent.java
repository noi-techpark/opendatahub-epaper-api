// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "scheduled_content")
public class ScheduledContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uuid;

    private Boolean disabled;

    private Integer eventId;

    private Date startDate;

    private Date endDate;

    private String eventDescription;

    private String spaceDesc;

    private String room;

    private boolean override;

    private boolean include;

    private String imageBase64;

    @ManyToOne
    private Display display;

    @ManyToOne
    private Template template;

    @OneToOne(mappedBy = "scheduledContent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private DisplayContent displayContent;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
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

    @PrePersist
    public void prePersist() {
        this.setUuid(UUID.randomUUID().toString());
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String description) {
        this.eventDescription = description;
    }

    public DisplayContent getDisplayContent() {
        return displayContent;
    }

    public void setDisplayContent(DisplayContent displayContent) {
        if (displayContent != null) {
            displayContent.setScheduledContent(this);
        }
        this.displayContent = displayContent;
    }

    public String getSpaceDesc() {
        return spaceDesc;
    }

    public void setSpaceDesc(String spaceDesc) {
        this.spaceDesc = spaceDesc;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public boolean getOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public boolean getInclude() {
        return include;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }

}