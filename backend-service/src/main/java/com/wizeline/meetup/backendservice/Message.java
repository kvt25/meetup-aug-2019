package com.wizeline.meetup.backendservice;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name="message")
@Entity

public class Message {
    @Id
    private Long id;

    private String name;

    private String message;

    @Column(name = "image_uri")
    private String imageUri;

    @Column(name = "image_labels")
    private String imageLabels;

    public Message() {
        this.id = System.currentTimeMillis();
    }
}
