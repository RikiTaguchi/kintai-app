package com.example.api.mapper.work;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.api.mapper.entity.work.WorkEntity;

@Mapper
public interface WorkMapper {

    List<WorkEntity> selectAll(UUID tutorId, LocalDate dateFrom, LocalDate dateTo);

    Optional<WorkEntity> select(UUID workId);

    boolean existsByTutorIdAndWorkingDate(
        @Param("tutorId") UUID tutorId,
        @Param("workingDate") LocalDate workingDate,
        @Param("excludeId") UUID excludeId
    );

    void insert(WorkEntity entity);

    void update(WorkEntity entity);

    void delete(UUID workId);

}
