package kr.co.bnk.bnk_project.repository;

import kr.co.bnk.bnk_project.entity.RiskTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskTestResultRepository extends JpaRepository<RiskTestResult, Long> {
    // 사용자 ID로 가장 최근의 투자 성향 검사 결과 조회
    Optional<RiskTestResult> findTopByCustNoOrderByTestDateDesc(String custNo);
}