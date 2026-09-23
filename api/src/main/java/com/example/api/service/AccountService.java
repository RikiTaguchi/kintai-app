package com.example.api.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.exception.AlreadyExistsException;
import com.example.api.exception.BusinessException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.AccountMapper;
import com.example.api.mapper.entity.AccountEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AccountEntity register(String loginId, String rawPassword) {

        if (accountMapper.selectByLoginId(loginId).isPresent()) {
            throw new AlreadyExistsException("このログインIDは既に使用されています");
        }

        String hashedPassword = passwordEncoder.encode(rawPassword);

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setLoginId(loginId);
        accountEntity.setPassword(hashedPassword);
        accountMapper.insert(accountEntity);

        return accountEntity;

    }

    @Transactional
    public void editLoginId(UUID accountId, String loginId) {

        AccountEntity accountEntity = accountMapper.select(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("アカウントが見つかりません"));

        if (!loginId.equals(accountEntity.getLoginId()) && accountMapper.selectByLoginId(loginId).isPresent()) {
            throw new AlreadyExistsException("このログインIDは既に使用されています");
        }

        accountEntity.setLoginId(loginId);
        accountMapper.update(accountEntity);

    }

    @Transactional
    public void changePassword(UUID accountId, String currentRaw, String newRaw) {

        AccountEntity accountEntity = accountMapper.select(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("アカウントが見つかりません"));

        if (!passwordEncoder.matches(currentRaw, accountEntity.getPassword())) {
            throw new BusinessException("現在のパスワードが正しくありません");
        }

        accountEntity.setPassword(passwordEncoder.encode(newRaw));
        accountMapper.update(accountEntity);

    }

    @Transactional
    public void resetPassword(UUID accountId, String newRaw) {

        AccountEntity accountEntity = accountMapper.select(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("アカウントが見つかりません"));

        accountEntity.setPassword(passwordEncoder.encode(newRaw));
        accountMapper.update(accountEntity);

    }

    @Transactional
    public void delete(UUID accountId) {
        accountMapper.delete(accountId);
    }

}
