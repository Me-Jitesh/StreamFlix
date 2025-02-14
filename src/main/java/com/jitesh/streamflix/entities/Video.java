package com.jitesh.streamflix.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Video {
    @Id
    private String id;

    private String name;

    @Lob
    @Column(length=100000*10000)
    private byte[] vid;

    @Lob
    @Column(length=100000*10000)
    private byte[] thumb;

}
