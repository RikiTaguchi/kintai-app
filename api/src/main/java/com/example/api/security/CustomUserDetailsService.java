package com.example.api.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.api.mapper.AccountMapper;
import com.example.api.service.dto.AccountDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final AccountMapper accountMapper;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        AccountDto account = accountMapper.selectByLoginId(loginId)
            .orElseThrow(() -> new UsernameNotFoundException("account not found"))
            .toDto();
        
        return new CustomUserDetails(account);
    }

}
