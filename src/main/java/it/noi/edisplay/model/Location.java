// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Entity class for Displays that contains all needed information for an
 * E-Display
 *
 * @Author Simon Dalvai
 */
@Entity
@Table(name = "locations")
@TypeDef(name = "string-array", typeClass = StringArrayType.class)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // @NotNull
    private String uuid;

    // @NotNull
    private String name;

    private String description;

    @Type(type = "string-array")
    @Column(name = "room_codes", columnDefinition = "text[]")
    private String[] roomCodes;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    public Location() {

    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @PrePersist
    public void prePersist() {
        this.setUuid(UUID.randomUUID().toString());
    }

    public String[] getRoomCodes() {
        return roomCodes;
    }

    public void setRoomCodes(String[] roomCodes) {
        this.roomCodes = roomCodes;
    }

}
