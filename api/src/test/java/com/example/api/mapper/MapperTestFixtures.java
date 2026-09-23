package com.example.api.mapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.example.api.mapper.entity.AccountEntity;
import com.example.api.mapper.entity.ManagerEntity;
import com.example.api.mapper.entity.TutorEntity;
import com.example.api.mapper.entity.template.LessonTemplateDetailEntity;
import com.example.api.mapper.entity.template.OfficeTemplateDetailEntity;
import com.example.api.mapper.entity.template.OtherTemplateDetailEntity;
import com.example.api.mapper.entity.template.TemplateEntity;
import com.example.api.mapper.entity.work.LessonWorkDetailEntity;
import com.example.api.mapper.entity.work.OfficeWorkDetailEntity;
import com.example.api.mapper.entity.work.OtherWorkDetailEntity;
import com.example.api.mapper.entity.work.WorkEntity;

/**
 * DB統合テスト共通のフィクスチャビルダ。
 * FK 順（accounts → managers/tutors → 子テーブル）を守って insert するための
 * シンプルなエンティティ生成を提供する。
 */
public final class MapperTestFixtures {

    public static final UUID CLASSROOM_TOZUKA = UUID.fromString("e6e04831-b165-4deb-b38b-dbda9c04bbd4");
    public static final UUID CLASSROOM_TOZUKA_EAST = UUID.fromString("08fac8b1-eeaf-4147-a69b-59bda54f71ff");

    private MapperTestFixtures() {}

    public static AccountEntity account(String loginId, UUID classroomId, UUID managerId, UUID tutorId) {
        AccountEntity e = new AccountEntity();
        e.setLoginId(loginId);
        e.setPassword("hashed");
        e.setClassroomId(classroomId);
        e.setManagerId(managerId);
        e.setTutorId(tutorId);
        return e;
    }

    public static ManagerEntity manager(UUID accountId, UUID classroomId, String loginId) {
        ManagerEntity e = new ManagerEntity();
        e.setAccountId(accountId);
        e.setLoginId(loginId);
        e.setPassword("hashed");
        e.setClassroomId(classroomId);
        e.setFirstName("太郎");
        e.setLastName("山田");
        return e;
    }

    public static TutorEntity tutor(UUID accountId, UUID classroomId, String loginId) {
        TutorEntity e = new TutorEntity();
        e.setAccountId(accountId);
        e.setLoginId(loginId);
        e.setPassword("hashed");
        e.setClassroomId(classroomId);
        e.setFirstName("花子");
        e.setLastName("佐藤");
        e.setTutorNumber(1);
        e.setTerminated(false);
        e.setTerminationDate(null);
        return e;
    }

    public static TemplateEntity template(UUID tutorId, UUID classroomId, String title) {
        TemplateEntity e = new TemplateEntity();
        e.setTutorId(tutorId);
        e.setClassroomId(classroomId);
        e.setTitle(title);
        e.setTransportationFee(100);
        return e;
    }

    public static LessonTemplateDetailEntity lessonTemplateDetail(UUID templateId) {
        LessonTemplateDetailEntity e = new LessonTemplateDetailEntity();
        e.setTemplateId(templateId);
        e.setStartTime(LocalTime.of(10, 0));
        e.setEndTime(LocalTime.of(12, 0));
        e.setBreakMinutes(0);
        e.setPeriodCodeM(true);
        e.setPeriodCodeK(false);
        e.setPeriodCodeS(false);
        e.setPeriodCodeA(false);
        e.setPeriodCodeB(false);
        e.setPeriodCodeC(false);
        e.setPeriodCodeD(false);
        return e;
    }

    public static OfficeTemplateDetailEntity officeTemplateDetail(UUID templateId) {
        OfficeTemplateDetailEntity e = new OfficeTemplateDetailEntity();
        e.setTemplateId(templateId);
        e.setStartTime(LocalTime.of(9, 0));
        e.setEndTime(LocalTime.of(10, 0));
        return e;
    }

    public static OtherTemplateDetailEntity otherTemplateDetail(UUID templateId) {
        OtherTemplateDetailEntity e = new OtherTemplateDetailEntity();
        e.setTemplateId(templateId);
        e.setStartTime(null);
        e.setEndTime(null);
        e.setBreakMinutes(0);
        e.setDescription(null);
        return e;
    }

    public static WorkEntity work(UUID tutorId, UUID classroomId, LocalDate date) {
        WorkEntity e = new WorkEntity();
        e.setTutorId(tutorId);
        e.setClassroomId(classroomId);
        e.setWorkingDate(date);
        e.setTransportationFee(100);
        return e;
    }

    public static LessonWorkDetailEntity lessonWorkDetail(UUID workId) {
        LessonWorkDetailEntity e = new LessonWorkDetailEntity();
        e.setWorkId(workId);
        e.setStartTime(LocalTime.of(10, 0));
        e.setEndTime(LocalTime.of(12, 0));
        e.setBreakMinutes(0);
        e.setPeriodCodeM(true);
        e.setPeriodCodeK(false);
        e.setPeriodCodeS(false);
        e.setPeriodCodeA(false);
        e.setPeriodCodeB(false);
        e.setPeriodCodeC(false);
        e.setPeriodCodeD(false);
        return e;
    }

    public static OfficeWorkDetailEntity officeWorkDetail(UUID workId) {
        OfficeWorkDetailEntity e = new OfficeWorkDetailEntity();
        e.setWorkId(workId);
        e.setStartTime(LocalTime.of(9, 0));
        e.setEndTime(LocalTime.of(10, 0));
        return e;
    }

    public static OtherWorkDetailEntity otherWorkDetail(UUID workId) {
        OtherWorkDetailEntity e = new OtherWorkDetailEntity();
        e.setWorkId(workId);
        e.setStartTime(null);
        e.setEndTime(null);
        e.setBreakMinutes(0);
        e.setDescription(null);
        return e;
    }
}
