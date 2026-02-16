package com.group12.athleticaX.controller;

import com.group12.athleticaX.model.Event;
import com.group12.athleticaX.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/all")
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}
