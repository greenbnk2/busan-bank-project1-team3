package kr.co.bnk.bnk_project.dto.admin;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListDTO {

    private String fundCode;        // 상품코드
    private String fundType;        // 유형
    private String fundName;        // 상품명
    private String operatorName;    // 회사명
    private LocalDateTime regDate;  // 등록일자
    private String operStatus;      // 상태 - 게시중, 수정중, 대기중, 중단

    // 추가
    private String categoryName; // 카테고리 이름 (한글)


}
