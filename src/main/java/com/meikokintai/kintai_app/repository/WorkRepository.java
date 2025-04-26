package com.meikokintai.kintai_app.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.meikokintai.kintai_app.model.Work;

public interface WorkRepository extends JpaRepository<Work, UUID>{
    
    @Query(nativeQuery = true, value = "SELECT * FROM works WHERE userid = :userid and date BETWEEN :datefrom AND :dateto ORDER BY date")
    List<Work> findByUserId(@Param("userid") UUID userid, @Param("datefrom") String datefrom, @Param("dateto") String dateto);
    
    @Query(nativeQuery = true, value = "SELECT * FROM works WHERE id = :id LIMIT 1")
    Work findWorkById(@Param("id") UUID id);

    @Query(nativeQuery = true, value = "SELECT * FROM works WHERE userid = :userid")
    List<Work> findAllByUserId(@Param("userid") UUID userid);
    
    @Query(nativeQuery = true, value = "SELECT SUM(classcount) FROM works WHERE userid = :userid and date BETWEEN :datefrom AND :dateto")
    int sumClassCount(@Param("userid") UUID userid, @Param("datefrom") String datefrom, @Param("dateto") String dateto);
    
    @Query(nativeQuery = true, value = "SELECT COUNT(date) FROM works WHERE userid = :userid and date BETWEEN :datefrom AND :dateto")
    int sumWorkDay(@Param("userid") UUID userid, @Param("datefrom") String datefrom, @Param("dateto") String dateto);
    
    @Query(nativeQuery = true, value = "SELECT SUM(outoftime) FROM works WHERE userid = :userid and date BETWEEN :datefrom AND :dateto")
    int sumOutOfTime(@Param("userid") UUID userid, @Param("datefrom") String datefrom, @Param("dateto") String dateto);
   
    @Query(nativeQuery = true, value = "SELECT SUM(officetime) FROM works WHERE userid = :userid and date BETWEEN :datefrom AND :dateto")
    int sumOfficeTime(@Param("userid") UUID userid, @Param("datefrom") String datefrom, @Param("dateto") String dateto);
   
    @Query(nativeQuery = true, value = "SELECT SUM(othertime) FROM works WHERE userid = :userid and date BETWEEN :datefrom AND :dateto")
    int sumOtherTime(@Param("userid") UUID userid, @Param("datefrom") String datefrom, @Param("dateto") String dateto);

    @Query(nativeQuery = true, value = "SELECT SUM(carfare) FROM works WHERE userid = :userid and date BETWEEN :datefrom AND :dateto")
    int sumCarfare(@Param("userid") UUID userid, @Param("datefrom") String datefrom, @Param("dateto") String dateto);
    
    @Query(nativeQuery = true, value = "SELECT SUM(overtime) FROM works WHERE userid = :userid and date BETWEEN :datefrom AND :dateto")
    int sumOverTime(@Param("userid") UUID userid, @Param("datefrom") String datefrom, @Param("dateto") String dateto);
    
    @Query(nativeQuery = true, value = "SELECT SUM(nighttime) FROM works WHERE userid = :userid and date BETWEEN :datefrom AND :dateto")
    int sumNightTime(@Param("userid") UUID userid, @Param("datefrom") String datefrom, @Param("dateto") String dateto);
    
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE works SET userid = :userid, date = :date, dayofweek = :dayofweek, timestart = :timestart, timeend = :timeend, classm = :classm, classk = :classk, classs = :classs, classa = :classa, classb = :classb, classc = :classc, classd = :classd, classcount = :classcount, helparea = :helparea, breaktime = :breaktime, officetimestart = :officetimestart, officetimeend = :officetimeend, officetime = :officetime, otherwork = :otherwork, othertimestart = :othertimestart, othertimeend = :othertimeend, otherbreaktime = :otherbreaktime, othertime = :othertime, carfare = :carfare, outoftime = :outoftime, overtime = :overtime, nighttime = :nighttime, supportsalary = :supportsalary where id = :id")
    void update(@Param("id") UUID id, @Param("userid") UUID userid, @Param("date") String date, @Param("dayofweek") String dayofweek, @Param("timestart") String timestart, @Param("timeend") String timeend, @Param("classm") Boolean classm, @Param("classk") Boolean classk, @Param("classs") Boolean classs, @Param("classa") Boolean classa, @Param("classb") Boolean classb, @Param("classc") Boolean classc, @Param("classd") Boolean classd, @Param("classcount") int classcount, @Param("helparea") String helparea, @Param("breaktime") int breaktime, @Param("officetimestart") String officetimestart, @Param("officetimeend") String officetimeend, @Param("officetime") int officetime, @Param("otherwork") String otherwork, @Param("othertimestart") String othertimestart, @Param("othertimeend") String othertimeend, @Param("otherbreaktime") int otherbreaktime, @Param("othertime") int othertime, @Param("carfare") int carfare, @Param("outoftime") int outoftime, @Param("overtime") int overtime, @Param("nighttime") int nighttime, @Param("supportsalary") String supportsalary);
    
}
