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

@Table(name = "salaries")
@Entity
@Getter
@Setter
public class Salary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "userid")
    private UUID userId;
    
    @Column(name = "datefrom")
    private String dateFrom;
    
    @Column(name = "classsalary")
    private int classSalary;
    
    @Column(name = "officesalary")
    private int officeSalary;
    
    @Column(name = "supportsalary")
    private int supportSalary;
    
    @Column(name = "carfare")
    private int carfare;
    
}
