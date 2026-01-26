package com.investment.backend.account.service;

import com.investment.backend.account.entity.Account;
import com.investment.backend.account.repository.AccountRepository;
import com.investment.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public void createPersonalAccount(User user) {
        // 이미 개인 계좌가 있는지 확인 (선택적)
        boolean exists = accountRepository.existsByUserAndTeamIsNullAndBattleIsNull(user);
        if (exists) {
            throw new IllegalArgumentException("이미 개인 계좌가 존재합니다.");
        }

        Account personalAccount = Account.createPersonalAccount(user);
        accountRepository.save(personalAccount);
    }
}
