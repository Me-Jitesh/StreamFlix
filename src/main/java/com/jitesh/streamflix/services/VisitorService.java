package com.jitesh.streamflix.services;

import com.jitesh.streamflix.entities.Visitor;

import java.util.List;

public interface VisitorService {
    public void saveVisitor(Visitor visitor);

    public Visitor getVisitor(Integer id);

    public List<Visitor> getAllVisitor();

    public void deleteVisitor(Integer id);

    public void deleteAllVisitor();

    public void saveAllVisitors(List<Visitor> visitors);
}
