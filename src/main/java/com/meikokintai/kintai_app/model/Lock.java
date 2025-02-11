package com.meikokintai.kintai_app.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name = "locks")
@Entity
@Getter
@Setter
public class Lock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "classareaid")
    private UUID classAreaId;
    
    @Column(name = "userid")
    private UUID userId;
    
    @Column(name = "year")
    private int year;

    @Column(name = "month")
    private int month;

    @Column(name = "status")
    private Boolean status;
    
}
