package com.jitesh.streamflix.repositories;

import com.jitesh.streamflix.entities.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorRepo extends JpaRepository<Visitor, Integer> {
}
