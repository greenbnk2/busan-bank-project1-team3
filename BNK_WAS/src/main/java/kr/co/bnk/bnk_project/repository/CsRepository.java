package kr.co.bnk.bnk_project.repository;

import kr.co.bnk.bnk_project.entity.Cs;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CsRepository extends JpaRepository<Cs, Long> {

    /**
     * FAQ 목록 조회 (JPA 메서드명 쿼리 사용 - 더 안전함)
     * Native Query 대신 JPA가 자동으로 생성하는 쿼리 사용
     */
    List<Cs> findAllByCategoryIdAndAnswerIsNotNull(Long categoryId);
    
    // Native Query 버전 (대체용 - 엔티티에 @Column 매핑이 필요함)
    // @Query(value =
    //         "SELECT * FROM CS c " +
    //                 "WHERE c.CATEGORY_ID = :categoryId " +
    //                 "AND c.ANSWER IS NOT NULL",
    //         nativeQuery = true)
    // List<Cs> findFaqListByCategoryId(@Param("categoryId") Long categoryId);
}