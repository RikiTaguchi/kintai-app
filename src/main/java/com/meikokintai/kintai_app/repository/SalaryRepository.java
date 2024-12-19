package com.meikokintai.kintai_app.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.meikokintai.kintai_app.model.Salary;


public interface SalaryRepository extends JpaRepository<Salary, UUID>{
    
    @Query(nativeQuery = true, value = "SELECT * FROM salaries WHERE userid = :userid ORDER BY datefrom")
    List<Salary> findByUserId(@Param("userid") UUID userid);
    
    @Query(nativeQuery = true, value = "SELECT * FROM salaries WHERE id = :salaryid")
    Salary getBySalaryId(@Param("salaryid") UUID salaryid);
    
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE salaries SET userid = :userid, datefrom = :datefrom, classsalary = :classsalary, officesalary = :officesalary, supportsalary = :supportsalary, carfare = :carfare WHERE id = :id")
    void update(@Param("id") UUID id, @Param("userid") UUID userid, @Param("datefrom") String datefrom, @Param("classsalary") int classsalary, @Param("officesalary") int officesalary, @Param("supportsalary") int supportsalary, @Param("carfare") int carfare);
    
}
