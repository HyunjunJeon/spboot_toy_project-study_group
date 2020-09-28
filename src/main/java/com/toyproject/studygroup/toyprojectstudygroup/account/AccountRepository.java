package com.toyproject.studygroup.toyprojectstudygroup.account;

import com.toyproject.studygroup.toyprojectstudygroup.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

@Transactional
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Account findByEmail(String email);
}
