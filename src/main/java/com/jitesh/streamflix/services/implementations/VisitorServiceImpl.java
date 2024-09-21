package com.jitesh.streamflix.services.implementations;

import com.jitesh.streamflix.entities.Visitor;
import com.jitesh.streamflix.repositories.VisitorRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitorServiceImpl implements VisitorService {
    @Autowired
    private VisitorRepo repo;

    @Override
    public void saveVisitor(Visitor visitor) {
        repo.save(visitor);
    }

    @Override
    public Visitor getVisitor(Integer id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Can't find visitor"));
    }

    @Override
    public List<Visitor> getAllVisitor() {
        return repo.findAll();
    }

    @Override
    public void deleteVisitor(Integer id) {
        repo.delete(getVisitor(id));
    }

    @Override
    public void deleteAllVisitor() {
        repo.deleteAll();
    }

    @Override
    public void saveAllVisitors(List<Visitor> visitors) {
        repo.saveAll(visitors);
    }
}
