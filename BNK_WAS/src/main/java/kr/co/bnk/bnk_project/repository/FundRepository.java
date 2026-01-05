package kr.co.bnk.bnk_project.repository;

import kr.co.bnk.bnk_project.entity.FundMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundRepository extends JpaRepository<FundMaster, String> {
    // 위험도(Risk Level)에 맞는 펀드 상품 목록 조회
    List<FundMaster> findAllByInvestGrade(String investGrade);
    
    /**
     * 펀드명 또는 특징에서 키워드 검색 (대소문자 무시)
     */
    List<FundMaster> findByFundNameContainingIgnoreCaseOrFundFeatureContainingIgnoreCase(
            String fundName, 
            String fundFeature
    );
    
    /**
     * 더 복잡한 검색이 필요한 경우 (JPQL 사용)
     */
    @Query("SELECT f FROM FundMaster f WHERE " +
           "LOWER(f.fundName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.fundFeature) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<FundMaster> searchFunds(@Param("keyword") String keyword);
}