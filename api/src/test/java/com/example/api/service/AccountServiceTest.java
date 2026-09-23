package com.example.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.api.exception.AlreadyExistsException;
import com.example.api.exception.BusinessException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.AccountMapper;
import com.example.api.mapper.entity.AccountEntity;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountMapper accountMapper;

    // PasswordEncoder は実物を使用（BCryptは決定的に動作するためモックより検証が容易）
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AccountService accountService;

    private UUID accountId;

    @BeforeEach
    void setUp() {
        // Mockito InjectMocks は final field なので、手動で構築してテスト用 encoder を注ぐ
        accountService = new AccountService(accountMapper, passwordEncoder);
        accountId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("パスワードは bcrypt でハッシュ化されて insert される")
        void hashesPassword() {
            when(accountMapper.selectByLoginId("user1")).thenReturn(Optional.empty());
            org.mockito.Mockito.doAnswer(inv -> {
                AccountEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return null;
            }).when(accountMapper).insert(any());

            AccountEntity saved = accountService.register("user1", "secret");

            assertEquals("user1", saved.getLoginId());
            assertNotEquals("secret", saved.getPassword());
            assertTrue(passwordEncoder.matches("secret", saved.getPassword()));
        }

        @Test
        @DisplayName("既に loginId が存在 → AlreadyExistsException")
        void duplicateLoginId() {
            AccountEntity existing = new AccountEntity();
            existing.setLoginId("user1");
            when(accountMapper.selectByLoginId("user1")).thenReturn(Optional.of(existing));

            assertThrows(AlreadyExistsException.class,
                () -> accountService.register("user1", "secret"));
            verify(accountMapper, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("editLoginId")
    class EditLoginId {

        @Test
        @DisplayName("アカウントが見つからない → ResourceNotFoundException")
        void missing() {
            when(accountMapper.select(accountId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                () -> accountService.editLoginId(accountId, "newid"));
        }

        @Test
        @DisplayName("loginId を変更する場合、既存の loginId と重複していれば AlreadyExistsException")
        void duplicate() {
            AccountEntity me = new AccountEntity();
            me.setId(accountId);
            me.setLoginId("oldid");
            when(accountMapper.select(accountId)).thenReturn(Optional.of(me));

            AccountEntity other = new AccountEntity();
            other.setLoginId("taken");
            when(accountMapper.selectByLoginId("taken")).thenReturn(Optional.of(other));

            assertThrows(AlreadyExistsException.class,
                () -> accountService.editLoginId(accountId, "taken"));
        }

        @Test
        @DisplayName("loginId が変更されない場合は重複チェックをスキップ")
        void skipsSelfCheck() {
            AccountEntity me = new AccountEntity();
            me.setId(accountId);
            me.setLoginId("same");
            when(accountMapper.select(accountId)).thenReturn(Optional.of(me));

            // selectByLoginId("same") が自分自身でも isPresent を返す可能性があるが、
            // loginId.equals(accountEntity.getLoginId()) = true なのでスキップされる
            accountService.editLoginId(accountId, "same");
            verify(accountMapper, never()).selectByLoginId(anyString());
            verify(accountMapper).update(me);
        }

        @Test
        @DisplayName("loginId が変更され、かつ未使用 → 更新される")
        void updates() {
            AccountEntity me = new AccountEntity();
            me.setId(accountId);
            me.setLoginId("oldid");
            when(accountMapper.select(accountId)).thenReturn(Optional.of(me));
            when(accountMapper.selectByLoginId("newid")).thenReturn(Optional.empty());

            accountService.editLoginId(accountId, "newid");
            assertEquals("newid", me.getLoginId());
            verify(accountMapper).update(me);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("現在パスワード不一致 → BusinessException")
        void wrongCurrentPassword() {
            AccountEntity me = new AccountEntity();
            me.setId(accountId);
            me.setPassword(passwordEncoder.encode("correct"));
            when(accountMapper.select(accountId)).thenReturn(Optional.of(me));

            BusinessException ex = assertThrows(BusinessException.class,
                () -> accountService.changePassword(accountId, "wrong", "new"));
            assertEquals("現在のパスワードが正しくありません", ex.getMessage());
            verify(accountMapper, never()).update(any());
        }

        @Test
        @DisplayName("現在パスワード一致 → ハッシュ更新して update")
        void updatesWhenCurrentMatches() {
            AccountEntity me = new AccountEntity();
            me.setId(accountId);
            me.setPassword(passwordEncoder.encode("correct"));
            when(accountMapper.select(accountId)).thenReturn(Optional.of(me));

            accountService.changePassword(accountId, "correct", "newpass");

            assertTrue(passwordEncoder.matches("newpass", me.getPassword()));
            verify(accountMapper).update(me);
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        @Test
        @DisplayName("アカウントが見つからない → ResourceNotFoundException")
        void missing() {
            when(accountMapper.select(accountId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                () -> accountService.resetPassword(accountId, "new"));
        }

        @Test
        @DisplayName("現在パスワード確認なしで更新される")
        void updatesWithoutCheck() {
            AccountEntity me = new AccountEntity();
            me.setId(accountId);
            me.setPassword(passwordEncoder.encode("any"));
            when(accountMapper.select(accountId)).thenReturn(Optional.of(me));

            accountService.resetPassword(accountId, "newpass");
            assertTrue(passwordEncoder.matches("newpass", me.getPassword()));
            verify(accountMapper).update(me);
        }
    }

    @Nested
    @DisplayName("delete")
    class Deletion {

        @Test
        @DisplayName("delete は委譲のみ")
        void delegates() {
            accountService.delete(accountId);
            verify(accountMapper).delete(accountId);
        }
    }
}
