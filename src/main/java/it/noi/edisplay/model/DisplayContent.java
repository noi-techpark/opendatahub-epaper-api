package it.noi.edisplay.model;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
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

    private String imageUrl;
    
    private String imageHash;
    
    @OneToOne
    @JoinColumn(name = "display_id", referencedColumnName = "id")
    private Display display;
    
    @OneToOne
    @JoinColumn(name = "template_id")
    private Template template;
    
    @OneToOne
    @JoinColumn(name = "scheduled_content_id", referencedColumnName = "id")
    private ScheduledContent scheduledContent;

    @OneToMany(mappedBy="displayContent", cascade=CascadeType.ALL)
    private List<ImageField> imageFields;

    public DisplayContent() {
    }

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

    @PrePersist
    public void prePersist() {
        this.setUuid(UUID.randomUUID().toString());
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageHash() {
        return imageHash;
    }

    public void setImageHash(String imageHash) {
        this.imageHash = imageHash;
    }

    public List<ImageField> getImageFields() {
        return imageFields;
    }

    public void setImageFields(List<ImageField> imageFields) {
        for (ImageField field : imageFields) { 
            field.setDisplayContent(this);
        }
        this.imageFields = imageFields;
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
