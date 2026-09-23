package com.example.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.ClassroomMapper;
import com.example.api.mapper.ManagerMapper;
import com.example.api.mapper.entity.AccountEntity;
import com.example.api.mapper.entity.ClassroomEntity;
import com.example.api.mapper.entity.ManagerEntity;
import com.example.api.service.dto.ManagerDto;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock private ManagerMapper managerMapper;
    @Mock private ClassroomMapper classroomMapper;
    @Mock private AccountService accountService;

    private ManagerService managerService;

    @BeforeEach
    void setUp() {
        managerService = new ManagerService(managerMapper, classroomMapper, accountService);
    }

    @Nested
    @DisplayName("find")
    class Find {

        @Test
        @DisplayName("存在しない → ResourceNotFoundException")
        void missing() {
            UUID id = UUID.randomUUID();
            when(managerMapper.select(id)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> managerService.find(id));
        }
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("AccountService.register を呼んでから manager insert")
        void createsAccountFirst() {
            UUID accountId = UUID.randomUUID();
            AccountEntity account = new AccountEntity();
            account.setId(accountId);
            when(accountService.register("m1", "raw")).thenReturn(account);

            // 登録後の select
            ManagerEntity saved = new ManagerEntity();
            saved.setId(UUID.randomUUID());
            saved.setAccountId(accountId);
            when(managerMapper.select(org.mockito.ArgumentMatchers.any())).thenReturn(Optional.of(saved));

            ManagerDto dto = new ManagerDto();
            dto.setLoginId("m1");
            dto.setPassword("raw");
            dto.setClassroomId(UUID.randomUUID());
            dto.setFirstName("first");
            dto.setLastName("last");

            managerService.register(dto);
            verify(accountService).register("m1", "raw");
        }
    }

    @Nested
    @DisplayName("edit")
    class Edit {

        @Test
        @DisplayName("管理者が見つからない → 404")
        void missing() {
            UUID id = UUID.randomUUID();
            ManagerDto dto = new ManagerDto();
            dto.setId(id);
            when(managerMapper.select(id)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> managerService.edit(dto));
        }

        @Test
        @DisplayName("教室が見つからない → 404")
        void classroomMissing() {
            UUID id = UUID.randomUUID();
            UUID classroomId = UUID.randomUUID();
            ManagerDto dto = new ManagerDto();
            dto.setId(id);
            dto.setClassroomId(classroomId);
            dto.setLoginId("m1");

            ManagerEntity manager = new ManagerEntity();
            manager.setId(id);
            manager.setAccountId(UUID.randomUUID());
            when(managerMapper.select(id)).thenReturn(Optional.of(manager));
            when(classroomMapper.select(classroomId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> managerService.edit(dto));
        }
    }

    @Nested
    @DisplayName("changePassword / delete")
    class PasswordAndDelete {

        @Test
        @DisplayName("changePassword: 管理者が見つからない → 404")
        void passwordManagerMissing() {
            UUID id = UUID.randomUUID();
            when(managerMapper.select(id)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                () -> managerService.changePassword(id, "old", "new"));
        }

        @Test
        @DisplayName("changePassword: 見つかったら AccountService.changePassword に委譲")
        void passwordDelegates() {
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            ManagerEntity m = new ManagerEntity();
            m.setId(id);
            m.setAccountId(accountId);
            when(managerMapper.select(id)).thenReturn(Optional.of(m));

            managerService.changePassword(id, "cur", "new");
            verify(accountService).changePassword(accountId, "cur", "new");
        }

        @Test
        @DisplayName("delete: 管理者が見つからない → 404")
        void deleteManagerMissing() {
            UUID id = UUID.randomUUID();
            when(managerMapper.select(id)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> managerService.delete(id));
        }

        @Test
        @DisplayName("delete: AccountService.delete(accountId) に委譲")
        void deleteDelegates() {
            UUID id = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            ManagerEntity m = new ManagerEntity();
            m.setId(id);
            m.setAccountId(accountId);
            when(managerMapper.select(id)).thenReturn(Optional.of(m));
            managerService.delete(id);
            verify(accountService).delete(accountId);
        }
    }
}
