package com.group12.athleticaX.repository;

import com.group12.athleticaX.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
