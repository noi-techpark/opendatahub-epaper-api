package it.noi.edisplay.model;


import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
//import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;


/**
 * Entity class for Displays that contains all needed information for an E-Display
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

	//Saves timestamp when logical display gets updated
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdate;

	//Saves timestamp when real physical display gets updated
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastRealDisplayUpdate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastState;

//	@NotNull
	@ManyToOne
	private Resolution resolution;

	@ManyToOne
	private Template template;

	// private byte[] image;

	private int batteryPercentage;

	public Display() {

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

	// public byte[] getImage() {
	// 	return image;
	// }

	// public void setImage(byte[] image) {
	// 	this.image = image;
	// }

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
}
