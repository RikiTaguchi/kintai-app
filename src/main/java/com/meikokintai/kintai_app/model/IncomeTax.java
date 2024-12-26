package com.meikokintai.kintai_app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name = "incometaxes")
@Entity
@Getter
@Setter
public class IncomeTax {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "min")
    private int min;
    
    @Column(name = "max")
    private int max;
    
    @Column(name = "tax")
    private int tax;
    
}
