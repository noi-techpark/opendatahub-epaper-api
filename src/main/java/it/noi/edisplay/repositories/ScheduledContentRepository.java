package it.noi.edisplay.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.noi.edisplay.model.ScheduledContent;

public interface ScheduledContentRepository extends JpaRepository<ScheduledContent, Integer> {

    ScheduledContent findByUuid(String uuid);

    List<ScheduledContent> findByDisplayId(int displayId);

    ScheduledContent findByDisplayIdAndEventId(int displayId, int eventId);

    List<ScheduledContent> findAll();
}
