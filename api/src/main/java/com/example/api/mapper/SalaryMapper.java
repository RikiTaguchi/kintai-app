package com.example.api.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.api.mapper.entity.SalaryEntity;

@Mapper
public interface SalaryMapper {

    List<SalaryEntity> selectAll(UUID tutorId);

    Optional<LocalDate> selectEarliestEffectiveDate(UUID tutorId);

    Optional<SalaryEntity> select(UUID salaryId);

    boolean existsByTutorIdAndEffectiveDate(
        @Param("tutorId") UUID tutorId,
        @Param("effectiveDate") LocalDate effectiveDate,
        @Param("excludeId") UUID excludeId
    );

    void insert(SalaryEntity entity);

    void update(SalaryEntity entity);

    void delete(UUID salaryId);

}
