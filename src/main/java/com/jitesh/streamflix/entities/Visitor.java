package com.jitesh.streamflix.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "visitor_table")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Visitor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String ip;
    private String country;
    private String phoneCode;
    private String region;
    private String city;
    private String continent;
    private double longitude;
    private double latitude;
    private String isp;
    private String flag;
    private String timezone;
    private Timestamp created;
}
