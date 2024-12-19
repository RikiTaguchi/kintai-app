package com.meikokintai.kintai_app.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.meikokintai.kintai_app.model.WorkTemplate;

public interface WorkTemplateRepository extends JpaRepository<WorkTemplate, UUID>{
    
    @Query(nativeQuery = true, value = "SELECT * FROM worktemplates WHERE id = :id LIMIT 1")
    WorkTemplate findTemplateById(@Param("id") UUID id);
    
    @Query(nativeQuery = true, value = "SELECT * FROM worktemplates WHERE userid = :userid")
    List<WorkTemplate> findByUserId(@Param("userid") UUID userid);
    
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE worktemplates SET title = :title, timestart = :timestart, timeend = :timeend, classm = :classm, classk = :classk, classs = :classs, classa = :classa, classb = :classb, classc = :classc, classd = :classd, breaktime = :breaktime, officetimestart = :officetimestart, officetimeend = :officetimeend, otherwork = :otherwork, othertimestart = :othertimestart, othertimeend = :othertimeend, otherbreaktime = :otherbreaktime, carfare = :carfare, helparea = :helparea where id = :id")
    void update(@Param("id") UUID id, @Param("title") String title, @Param("timestart") String timestart, @Param("timeend") String timeend, @Param("classm") Boolean classm, @Param("classk") Boolean classk, @Param("classs") Boolean classs, @Param("classa") Boolean classa, @Param("classb") Boolean classb, @Param("classc") Boolean classc, @Param("classd") Boolean classd, @Param("breaktime") int breaktime, @Param("officetimestart") String officetimestart, @Param("officetimeend") String officetimeend, @Param("otherwork") String otherwork, @Param("othertimestart") String othertimestart, @Param("othertimeend") String othertimeend, @Param("otherbreaktime") int otherbreaktime, @Param("carfare") int carfare, @Param("helparea") String heplarea);
    
}
