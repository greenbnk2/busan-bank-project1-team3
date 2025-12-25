package kr.co.bnk.bnk_project.dto;

import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Arrays;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageRequestDTO {

    @Builder.Default
    private int pg = 1;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String cate = "free";

    private String searchType;
    private String keyword;

    // 펀드 목록 검색 추가
    private String category;
    private String status;
    private String requester;


    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Builder.Default
    private LocalDate baseDate = LocalDate.now();

    @Builder.Default
    private String sort = "sales_sum";

    @Builder.Default
    private String dir = "desc";

    private String company;      // 택배사
    private String dateFrom;     // 'YYYY-MM-DD'
    private String dateTo;       // 'YYYY-MM-DD'




    private String sellerId;   // 로그인 아이디
    private String userType;   // 관리자/판매자/일반



    // 파생값은 필드로 보관하지 않고 계산만
    public int getOffset() {
        int safePg = Math.max(pg, 1);
        int safeSize = Math.max(size, 1);
        return (safePg - 1) * safeSize;
    }
    public int getStartRow() {
        return getOffset();
    }

    public int getEndRow() {
        return getOffset() + Math.max(size, 1);
    }
    /** 단일 정렬 컬럼 */
    public Pageable getPageable(String sort){
        int safePg = Math.max(pg, 1) - 1;
        int safeSize = Math.max(size, 1);
        return PageRequest.of(safePg, safeSize, Sort.by(sort).descending());
    }

    /** 다중 정렬 (선택) */
    public Pageable getPageable(String... sorts){
        int safePg = Math.max(pg, 1) - 1;
        int safeSize = Math.max(size, 1);
        Sort s = Sort.by(Arrays.stream(sorts).map(Sort::by).map(Sort::descending).flatMap(Sort::stream).toList());
        return PageRequest.of(safePg, safeSize, s);
    }
}