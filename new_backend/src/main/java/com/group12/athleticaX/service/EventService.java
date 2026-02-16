package com.group12.athleticaX.service;

import com.group12.athleticaX.model.Event;
import com.group12.athleticaX.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    // Get all events
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // Create event
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    // Get event by ID
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    // Update event
    public Event updateEvent(Long id, Event updatedEvent) {

        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        existingEvent.setName(updatedEvent.getName());
        existingEvent.setDate(updatedEvent.getDate());  // LocalDate
        existingEvent.setLocation(updatedEvent.getLocation());

        return eventRepository.save(existingEvent);
    }

    // Delete event
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
}
