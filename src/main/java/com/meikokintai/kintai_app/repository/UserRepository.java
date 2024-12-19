package com.meikokintai.kintai_app.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.meikokintai.kintai_app.model.User;


public interface UserRepository extends JpaRepository<User, UUID>{
    
    @Query(nativeQuery = true, value = "SELECT * FROM users WHERE classareaid = :classareaid")
    List<User> findByClassAreaId(@Param("classareaid") UUID classareaid);
    
    @Query(nativeQuery = true, value = "SELECT * FROM users WHERE id = :userid LIMIT 1")
    User getByUserId(@Param("userid") UUID userid);
    
    @Query(nativeQuery = true, value = "SELECT * FROM users WHERE loginid = :loginid LIMIT 1")
    User getByLoginId(@Param("loginid") String loginid);
    
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE users SET id = :id, loginid = :loginid, username = :username, password = :password, classareaid = :classareaid WHERE id = :id")
    void update(@Param("id") UUID id, @Param("loginid") String loginid, @Param("username") String username, @Param("password") String password, @Param("classareaid") UUID classareaid);
    
}
