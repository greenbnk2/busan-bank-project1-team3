package kr.co.bnk.bnk_project.repository;

import kr.co.bnk.bnk_project.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCustId(String custId);

    boolean existsByCustId(String custId);
}