package com.meikokintai.kintai_app.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.meikokintai.kintai_app.model.Lock;
import java.util.List;



public interface LockRepository extends JpaRepository<Lock, UUID>{
    
    @Query(nativeQuery = true, value = "SELECT * FROM locks WHERE classareaid = :classareaid AND userid = :userid AND year = :year AND month = :month LIMIT 1")
    Lock getByTarget(@Param("classareaid") UUID classareaid, @Param("userid") UUID userid, @Param("year") int year, @Param("month") int month);
    
    @Query(nativeQuery = true, value = "SELECT * FROM locks WHERE userid = :userid")
    List<Lock> findByUserId(@Param("userid") UUID userid);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE locks SET status = :status WHERE classareaid = :classareaid AND userid = :userid AND year = :year AND month = :month")
    void update(@Param("classareaid") UUID classareaid, @Param("userid") UUID userid, @Param("year") int year, @Param("month") int month, @Param("status") Boolean status);
    
}
