package com.ap.repository;

import com.ap.entity.FundMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundRepository extends JpaRepository<FundMaster, String> {
    // 위험도(Risk Level)에 맞는 펀드 상품 목록 조회
    List<FundMaster> findAllByInvestGrade(String investGrade);

    String fundId(String fundId);
}
