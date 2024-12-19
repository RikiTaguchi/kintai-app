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

@Table(name = "works")
@Entity
@Setter
@Getter
public class Work {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "userid")
    private UUID userId;
    
    @Column(name = "date")
    private String date;
    
    @Column(name = "dayofweek")
    private String dayOfWeek;
    
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
    
    @Column(name = "classcount")
    private int classCount;
    
    @Column(name = "breaktime")
    private int breakTime;
    
    @Column(name = "officetimestart")
    private String officeTimeStart;
    
    @Column(name = "officetimeend")
    private String officeTimeEnd;
    
    @Column(name = "officetime")
    private int officeTime;
    
    @Column(name = "otherwork")
    private String otherWork;
    
    @Column(name = "othertimestart")
    private String otherTimeStart;
    
    @Column(name = "othertimeend")
    private String otherTimeEnd;
    
    @Column(name = "otherbreaktime")
    private int otherBreakTime;
    
    @Column(name = "othertime")
    private int otherTime;
    
    @Column(name = "carfare")
    private int carfare;
    
    @Column(name = "supportsalary")
    private String supportSalary;
    
    @Column(name = "helparea")
    private String helpArea;
    
    @Column(name = "outoftime")
    private int outOfTime;
    
    @Column(name = "overtime")
    private int overTime;
    
    @Column(name = "nighttime")
    private int nightTime;
    
}
