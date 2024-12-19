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

@Table(name = "worktemplates")
@Entity
@Setter
@Getter
public class WorkTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "userid")
    private UUID userId;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "timestart")
    private String timeStart;
    
    @Column(name = "timeend")
    private String timeEnd;
    
    @Column(name = "classm")
    private Boolean classM;
    
    @Column(name = "classk")
    private Boolean classK;
    
    @Column(name = "classs")
    private Boolean classS;
    
    @Column(name = "classa")
    private Boolean classA;
    
    @Column(name = "classb")
    private Boolean classB;
    
    @Column(name = "classc")
    private Boolean classC;
    
    @Column(name = "classd")
    private Boolean classD;
    
    @Column(name = "breaktime")
    private int breakTime;
    
    @Column(name = "officetimestart")
    private String officeTimeStart;
    
    @Column(name = "officetimeend")
    private String officeTimeEnd;
    
    @Column(name = "otherwork")
    private String otherWork;
    
    @Column(name = "othertimestart")
    private String otherTimeStart;
    
    @Column(name = "othertimeend")
    private String otherTimeEnd;
    
    @Column(name = "otherbreaktime")
    private int otherBreakTime;
    
    @Column(name = "carfare")
    private int carfare;
    
    @Column(name = "helparea")
    private String helpArea;
    
}
