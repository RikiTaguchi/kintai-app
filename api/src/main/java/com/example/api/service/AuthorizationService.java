package com.example.api.service;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.api.exception.AuthorizationFailedException;
import com.example.api.exception.InvalidInputException;
import com.example.api.security.CustomUserDetails;
import com.example.api.service.dto.TutorDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final TutorService tutorService;

    public void assertManagerSelf(CustomUserDetails userDetails, UUID managerId) {
        if (!managerId.equals(userDetails.getId())) {
            throw new AuthorizationFailedException("アクセス権限がありません");
        }
    }

    public void assertTutorSelf(CustomUserDetails userDetails, UUID tutorId) {
        if (!tutorId.equals(userDetails.getId())) {
            throw new AuthorizationFailedException("アクセス権限がありません");
        }
    }

    public void assertPathId(UUID pathId, UUID requestId) {
        if (!Objects.equals(pathId, requestId)) {
            throw new InvalidInputException("不正なリクエストパラメータです");
        }
    }

    public void assertResourceBelongsToTutor(UUID tutorId, UUID resourceTutorId) {
        if (!Objects.equals(tutorId, resourceTutorId)) {
            throw new AuthorizationFailedException("アクセス権限がありません");
        }
    }

    public void assertManagerClassroom(CustomUserDetails userDetails, UUID classroomId) {
        if (!userDetails.getClassroomId().equals(classroomId)) {
            throw new AuthorizationFailedException("アクセス権限がありません");
        }
    }

    public void assertManagerOwnsTutor(CustomUserDetails userDetails, UUID tutorId) {
        assertManagerOwnsTutor(userDetails, tutorService.find(tutorId));
    }

    public void assertManagerOwnsTutor(CustomUserDetails userDetails, TutorDto tutor) {
        if (!tutor.getClassroomId().equals(userDetails.getClassroomId())) {
            throw new AuthorizationFailedException("アクセス権限がありません");
        }
    }

    public void assertTutorAccess(CustomUserDetails userDetails, UUID tutorId) {
        assertTutorAccess(userDetails, tutorService.find(tutorId));
    }

    public void assertTutorAccess(CustomUserDetails userDetails, TutorDto tutor) {
        if (userDetails.isManager()) {
            assertManagerOwnsTutor(userDetails, tutor);
        } else if (userDetails.isTutor()) {
            assertTutorSelf(userDetails, tutor.getId());
        } else {
            throw new AuthorizationFailedException("アクセス権限がありません");
        }
    }

}
