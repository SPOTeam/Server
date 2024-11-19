package com.example.spot.repository;

import com.example.spot.domain.Member;
import java.util.Optional;

import com.example.spot.domain.enums.LoginType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByEmailAndLoginTypeNot(String email, LoginType loginType);

    boolean existsByEmailAndLoginType(String email, LoginType loginType);

    Optional<Member> findByEmailAndLoginType(String email, LoginType loginType);
}
