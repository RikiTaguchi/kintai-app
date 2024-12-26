package com.meikokintai.kintai_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.meikokintai.kintai_app.model.IncomeTax;

public interface IncomeTaxRepository extends JpaRepository<IncomeTax, Integer>{

    @Query(nativeQuery = true, value = "SELECT * FROM incometaxes WHERE min <= :tax AND :tax < max LIMIT 1")
    IncomeTax getByTotalIncome(@Param("tax") int tax);

}
