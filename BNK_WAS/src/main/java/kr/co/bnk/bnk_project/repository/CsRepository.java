package kr.co.bnk.bnk_project.repository;

import kr.co.bnk.bnk_project.entity.Cs;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CsRepository extends JpaRepository<Cs, Long> {

    @Query(value =
            "SELECT * FROM CS c " +
                    "WHERE c.CATEGORY_ID = :categoryId " +
                    "AND c.ANSWER IS NOT NULL",
            nativeQuery = true)
    List<Cs> findFaqListByCategoryId(@Param("categoryId") Long categoryId);

    //List<Cs> findAllByCategoryIdAndAnswerIsNotNull(Long categoryId);
}