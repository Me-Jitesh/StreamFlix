package com.jitesh.streamflix.controllers;

import com.jitesh.streamflix.entities.Visitor;
import com.jitesh.streamflix.services.VisitorService;
import com.jitesh.streamflix.utils.IPLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/visitor")
@CrossOrigin("*")
public class VisitorController {

    @Autowired
    private VisitorService visitorService;

    @GetMapping("/save/{ip}")
    public ResponseEntity<?> setVisitorLocation(@PathVariable String ip) {
        Visitor visitor = IPLocation.getGeoLocation(ip);
        visitorService.saveVisitor(visitor);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get/{id}")
    public Visitor getVisitors(@PathVariable Integer id) {
        return visitorService.getVisitor(id);
    }

    @GetMapping("/all")
    public List<Visitor> getAllVisitors() {
        return visitorService.getAllVisitor();
    }

    @GetMapping("/delete/{id}")
    public ResponseEntity<?> deleteVisitor(@PathVariable Integer id) {
        visitorService.deleteVisitor(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/delete/all")
    public ResponseEntity<?> deleteAllVisitor() {
        visitorService.deleteAllVisitor();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/save/all")
    public ResponseEntity<?> saveAllVisitors(List<Visitor> visitors) {
        visitorService.saveAllVisitors(visitors);
        return ResponseEntity.ok().build();
    }
}
