package it.noi.edisplay.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "display_content")
public class DisplayContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uuid;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;
    
    @OneToOne
    @JoinColumn(name = "display_id", referencedColumnName = "id")
    private Display display;
    
    @OneToOne
    @JoinColumn(name = "template_id")
    private Template template;
    
    @OneToOne
    @JoinColumn(name = "scheduled_content_id", referencedColumnName = "id")
    private ScheduledContent scheduledContent;

    @OneToMany(mappedBy="displayContent", cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval = true)
    private List<ImageField> imageFields;

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

    public List<ImageField> getImageFields() {
        return imageFields;
    }

    public void setImageFields(List<ImageField> imageFields) {
        for (ImageField field : imageFields) { 
            field.setDisplayContent(this);
        }
        
        if (this.imageFields == null) {
            this.imageFields = imageFields;
        } else { //If the list already exists, we have to modify it otherwise Hibernate will not work properly
            this.imageFields.clear();
            if (imageFields != null) {
                this.imageFields.addAll(imageFields);
            }
        }
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public ScheduledContent getScheduledContent() {
        return scheduledContent;
    }

    public void setScheduledContent(ScheduledContent scheduledContent) {
        this.scheduledContent = scheduledContent;
    }
}
