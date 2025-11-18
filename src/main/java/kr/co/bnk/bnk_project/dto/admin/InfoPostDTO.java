package kr.co.bnk.bnk_project.dto.admin;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class InfoPostDTO {

    private int postId;
    private String postType;
    private String title;
    private String summary;
    private String content;
    private String status;
    private LocalDateTime publishStartAt;
    private LocalDateTime publishEndAt;
    private String channels;
    private int categoryId;    // 게시판 구분
    private int versionOn;
    private String isLatest;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    // 추가 컬럼
    private String disclosureType; // 공시자료 내부 분류 : 운용보고서, 안내사항 등.
    private String adhocType;// 수시공시 내부 분류 : 중요공시, 일반공시, 긴급공시 등.
    private String fundInfoType; // 펀드정보 내부 분류 : 주식형, 채권형 등.
    private String guideType; // 펀드 가이드 내부 분류 : 투자안내, 상품안내 등.
    private String marketType; // 펀드시황 내부 분류 : 주간 리포트, 월간전망, 시장속보 등.

    private String adhocNo; // 수시공시번호
    private String fundInfoCode; // 펀드정보 , 코드
}
