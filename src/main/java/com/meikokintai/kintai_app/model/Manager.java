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

@Table(name = "managers")
@Entity
@Setter
@Getter
public class Manager {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "loginid")
    private String loginId;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "classarea")
    private String classArea;

    @Column(name = "classcode")
    private int classCode;
    
}
