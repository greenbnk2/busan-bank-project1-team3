package kr.co.bnk.bnk_project.controller;

import kr.co.bnk.bnk_project.dto.FundMasterDTO;
import kr.co.bnk.bnk_project.dto.MyFundResponse;
import kr.co.bnk.bnk_project.dto.ProductDTO;
import kr.co.bnk.bnk_project.dto.RiskTestResultDTO;
import kr.co.bnk.bnk_project.dto.mobile.FundOrderDTO;
import kr.co.bnk.bnk_project.dto.mobile.FundSubscriptionRequestDTO;
import kr.co.bnk.bnk_project.exception.DuplicateFundSubscriptionException;
import kr.co.bnk.bnk_project.security.MyUserDetails;
import kr.co.bnk.bnk_project.service.FundService;
import kr.co.bnk.bnk_project.service.MyFundService;
import kr.co.bnk.bnk_project.mapper.RiskTestMapper;
import kr.co.bnk.bnk_project.mapper.mobile.FundOrderMapper;
import kr.co.bnk.bnk_project.service.FundService;
import kr.co.bnk.bnk_project.service.InvestmentService;
import kr.co.bnk.bnk_project.service.mobile.FundOrderFixService;
import kr.co.bnk.bnk_project.service.mobile.FundOrderStartService;
import kr.co.bnk.bnk_project.service.mobile.FundSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* ====================================================================
 * [2024-12-24 추가] Flutter 앱 전용 펀드 API 컨트롤러
 * ====================================================================
 * Flutter 앱에서 사용할 REST API 엔드포인트 제공
 * 경로: /api/funds
 * ==================================================================== */
/**
 * Flutter 앱 전용 펀드 API 컨트롤러
 * /api/funds 경로로 매핑
 */
@RestController
@RequestMapping("/api/funds")
@RequiredArgsConstructor
@Slf4j
public class FlutterFundController {

    private final FundService fundService;
    private final FundSubscriptionService fundSubscriptionService;
    private final MyFundService myFundService;
    private final InvestmentService investmentService;
    private final FundOrderMapper fundOrderMapper;
    private final FundOrderFixService fundOrderFixService;
    private final FundOrderStartService fundOrderStartService;
    private final RiskTestMapper riskTestMapper;

    /**
     * 판매량 Best 펀드 목록
     * (현재는 전체 목록 상위 10개 반환, 추후 판매량 기준 정렬 로직 추가 필요)
     */
    @GetMapping("/category/sales")
    public ResponseEntity<List<ProductDTO>> getSalesFunds() {
        List<ProductDTO> funds = fundService.getProductList();
        // TODO: 판매량 기준 정렬 로직 추가 필요
        // 현재는 전체 목록 상위 10개 반환
        List<ProductDTO> result = funds.stream().limit(10).toList();
        
        // 디버깅: 처음 3개 펀드의 fundcode 확인
        if (!result.isEmpty()) {
            log.info("=== 펀드 목록 응답 디버깅 (sales) ===");
            for (int i = 0; i < Math.min(3, result.size()); i++) {
                ProductDTO fund = result.get(i);
                log.info("펀드 #{}: fundcode={}, fundNm={}", i, fund.getFundcode(), fund.getFundNm());
            }
            log.info("======================================");
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 수익률 Best 펀드 목록
     * (기존 getFundYieldBest() 메서드 활용)
     */
    @GetMapping("/category/yield")
    public ResponseEntity<List<ProductDTO>> getYieldFunds() {
        List<ProductDTO> funds = fundService.getFundYieldBest();
        
        return ResponseEntity.ok(funds);
    }

    /**
     * 펀드 검색 API (FundMaster 테이블 사용)
     * GET /api/funds/search?keyword={keyword}
     */
    @GetMapping("/search")
    public ResponseEntity<List<FundMasterDTO>> searchFunds(
            @RequestParam String keyword
    ) {
        List<FundMasterDTO> funds = fundService.searchFundsFromMaster(keyword);
        return ResponseEntity.ok(funds);
    }

    /**
     * 모든 카테고리 통합 조회
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, List<ProductDTO>>> getAllFunds() {
        Map<String, List<ProductDTO>> allFunds = new HashMap<>();
        allFunds.put("sales", fundService.getProductList().stream().limit(10).toList());
        allFunds.put("yield", fundService.getFundYieldBest());
        
        return ResponseEntity.ok(allFunds);
    }

    /**
     * 투자성향 조사 완료 여부 확인
     * Flutter 앱에서 투자성향 조사를 이미 했는지 확인합니다.
     */
    @GetMapping("/risk-test/check")
    public ResponseEntity<Map<String, Object>> checkRiskTest() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: 로그인 기능 완성 후 세션에서 가져오기
            Long custNo = 18L;
            
            // 오늘 투자성향 조사를 했는지 확인
            boolean hasTodayRiskTest = investmentService.hasTodayRiskTest(custNo);
            
            // 유효한 투자성향 조사 결과 조회 (오늘 또는 만료되지 않은 것)
            RiskTestResultDTO latestResult = null;
            if (hasTodayRiskTest) {
                latestResult = riskTestMapper.findValidTestByCustNo(custNo);
            }
            
            response.put("hasCompletedToday", hasTodayRiskTest);
            if (latestResult != null) {
                Map<String, Object> resultInfo = new HashMap<>();
                resultInfo.put("testRunId", latestResult.getTestRunId());
                resultInfo.put("totalScore", latestResult.getTotalScore());
                resultInfo.put("riskType", latestResult.getRiskType());
                // testDate는 DTO에 필드가 없으므로 제외
                response.put("latestResult", resultInfo);
            } else {
                response.put("latestResult", null);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("투자성향 조사 확인 중 오류 발생", e);
            // 에러가 나도 false 반환하여 조사 가능하도록
            response.put("hasCompletedToday", false);
            response.put("latestResult", null);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 투자성향 조사 결과 저장
     * Flutter 앱에서 투자성향 조사를 완료한 후 결과를 저장합니다.
     */
    @PostMapping("/risk-test/save")
    public ResponseEntity<Map<String, Object>> saveRiskTestResult(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: 로그인 기능 완성 후 세션에서 가져오기
            Long custNo = 18L;
            
            // 요청 데이터에서 점수와 성향 타입 추출
            Object totalScoreObj = request.get("totalScore");
            Object riskTypeObj = request.get("riskType");
            
            Integer totalScore = null;
            String riskType = null;
            
            // totalScore 변환 (int 또는 Integer 모두 처리)
            if (totalScoreObj instanceof Integer) {
                totalScore = (Integer) totalScoreObj;
            } else if (totalScoreObj instanceof Number) {
                totalScore = ((Number) totalScoreObj).intValue();
            }
            
            // riskType 변환
            if (riskTypeObj instanceof String) {
                riskType = (String) riskTypeObj;
            }
            
            if (totalScore == null || riskType == null) {
                response.put("success", false);
                response.put("message", "필수 파라미터가 누락되었습니다. (totalScore, riskType)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // DB 저장 (서비스에서 중복 체크 포함)
            investmentService.saveRiskTestResult(custNo, totalScore, riskType);
            
            response.put("success", true);
            response.put("message", "투자성향 조사 결과가 저장되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "투자성향 조사 결과 저장 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 펀드 가입
     * 가입 전 투자성향 조사 여부를 확인하고, 
     * 오늘 조사를 하지 않은 경우 조사 필요 플래그를 반환합니다.
     */
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribeFund(@RequestBody FundSubscriptionRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        // 디버깅: 요청 데이터 확인
        log.info("=== 펀드 가입 요청 디버깅 ===");
        log.info("요청 fundCode: {}", request.getFundCode());
        log.info("요청 amount: {}", request.getAmount());
        log.info("요청 investmentType: {}", request.getInvestmentType());
        log.info("============================");

        try {
            // TODO: 로그인 기능 완성 후 세션에서 가져오기
            Long custNo = 18L;
            
            // 투자성향 조사 여부 확인 (오늘 한 번이라도 했는지)
            boolean hasTodayRiskTest = investmentService.hasTodayRiskTest(custNo);
            
            if (!hasTodayRiskTest) {
                // 오늘 투자성향 조사를 하지 않은 경우, 조사 필요 플래그 반환
                response.put("success", false);
                response.put("message", "펀드 가입을 위해서는 먼저 투자성향 조사를 완료해야 합니다.");
                response.put("requiresRiskTest", true);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 투자성향 조사를 이미 완료한 경우, 펀드 가입 진행
            Long orderId = fundSubscriptionService.subscribeFund(request);

            response.put("success", true);
            response.put("orderId", orderId);
            response.put("message", "펀드 가입이 완료되었습니다.");

            return ResponseEntity.ok(response);
        } catch (DuplicateFundSubscriptionException e) {
            // 중복 가입 예외 (기존 가입 정보 포함)
            response.put("success", false);
            response.put("message", e.getMessage());
            if (e.getExistingSubscription() != null) {
                Map<String, Object> existingInfo = new HashMap<>();
                existingInfo.put("amount", e.getExistingSubscription().getAmount());
                if (e.getExistingSubscription().getStartAt() != null) {
                    existingInfo.put("startAt", e.getExistingSubscription().getStartAt()
                            .format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
                }
                response.put("existingSubscription", existingInfo);
            }
            return ResponseEntity.badRequest().body(response);
        } catch (IllegalStateException e) {
            // 기타 IllegalStateException (오늘 이미 조사 완료 등)
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "펀드 가입 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 보유펀드 목록 조회
     * GET /api/funds/my
     * 
     * 로그인한 사용자 또는 파라미터에서 사용자 정보를 추출하여 해당 사용자의 보유펀드 목록을 반환
     * - 보유중(HOLDING): 체결 반영 완료된 펀드
     * - 신청중(PENDING): 체결 전/진행중인 펀드
     * - 로그인이 안되어있으면 custNo=18로 기본값 설정
     */
    @GetMapping("/my")
    public ResponseEntity<List<MyFundResponse>> getMyFunds(
            @RequestParam(required = false) Integer custNo,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        // custNo가 파라미터로 없으면 로그인한 사용자 정보에서 가져오기
        if (custNo == null) {
            if (userDetails != null && userDetails.getUserDTO() != null) {
                // 로그인한 사용자가 있으면 해당 사용자의 custNo 사용
                custNo = userDetails.getUserDTO().getCustNo().intValue();
            } else {
                // 로그인이 안되어있으면 기본값 18 사용
                custNo = 18;
            }
        }
        
        List<MyFundResponse> myFunds = myFundService.getMyFunds(custNo);
        return ResponseEntity.ok(myFunds);
    }

    /**
     * 보유펀드 상세 정보 조회
     * GET /api/funds/my/{fundCode}/detail
     */
    @GetMapping("/my/{fundCode}/detail")
    public ResponseEntity<Map<String, Object>> getMyFundDetail(
            @PathVariable String fundCode,
            @RequestParam(required = false) Integer custNo,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        if (custNo == null) {
            if (userDetails != null && userDetails.getUserDTO() != null) {
                custNo = userDetails.getUserDTO().getCustNo().intValue();
            } else {
                custNo = 18; // 기본값
            }
        }
        
        try {
            Map<String, Object> detail = myFundService.getMyFundDetail(custNo, fundCode);
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 보유펀드 수익률 히스토리 조회
     * GET /api/funds/my/{fundCode}/profit-history?period={period}
     * period: '1M', '3M', '6M', '1Y', 'ALL'
     */
    @GetMapping("/my/{fundCode}/profit-history")
    public ResponseEntity<List<Map<String, Object>>> getMyFundProfitHistory(
            @PathVariable String fundCode,
            @RequestParam(defaultValue = "ALL") String period,
            @RequestParam(required = false) Integer custNo,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        if (custNo == null) {
            if (userDetails != null && userDetails.getUserDTO() != null) {
                custNo = userDetails.getUserDTO().getCustNo().intValue();
            } else {
                custNo = 18; // 기본값
            }
        }
        
        try {
            List<Map<String, Object>> history = myFundService.getMyFundProfitHistory(custNo, fundCode, period);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 보유펀드 거래 내역 조회
     * GET /api/funds/my/{fundCode}/transactions
     */
    @GetMapping("/my/{fundCode}/transactions")
    public ResponseEntity<List<Map<String, Object>>> getMyFundTransactions(
            @PathVariable String fundCode,
            @RequestParam(required = false) Integer custNo,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        if (custNo == null) {
            if (userDetails != null && userDetails.getUserDTO() != null) {
                custNo = userDetails.getUserDTO().getCustNo().intValue();
            } else {
                custNo = 18; // 기본값
            }
        }

        try {
            List<Map<String, Object>> transactions = myFundService.getMyFundTransactions(custNo, fundCode);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
     /*
     * 주문 상태 조회
=======
     /* 주문 상태 조회
>>>>>>> Stashed changes
     * 주문 ID로 주문의 현재 상태와 상세 정보를 조회합니다.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderStatus(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();

        try {
            FundOrderDTO order = fundOrderMapper.getOrderById(orderId);

            if (order == null) {
                response.put("success", false);
                response.put("message", "주문을 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }

            // 상태 정보 구성
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("orderId", order.getOrderId());
            orderInfo.put("status", order.getStatus());
            orderInfo.put("statusName", getStatusName(order.getStatus()));
            orderInfo.put("fundCode", order.getFundCode());
            orderInfo.put("reqAmount", order.getReqAmount());
            orderInfo.put("fixAmount", order.getFixAmount());
            orderInfo.put("requestAt", order.getRequestAt() != null ? 
                    order.getRequestAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            orderInfo.put("amountFixAt", order.getAmountFixAt() != null ? 
                    order.getAmountFixAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            orderInfo.put("startAt", order.getStartAt() != null ? 
                    order.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);

            // 상태별 예상 일정 계산
            Map<String, String> schedule = calculateSchedule(order);
            orderInfo.put("schedule", schedule);

            response.put("success", true);
            response.put("order", orderInfo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("주문 상태 조회 실패: orderId={}, error={}", orderId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "주문 상태 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 상태명 한글 변환
     */
    private String getStatusName(String status) {
        if (status == null) return "알 수 없음";
        
        return switch (status) {
            case "REQUESTED" -> "투자신청";
            case "FIXED" -> "금액확정";
            case "STARTED" -> "투자시작";
            case "CANCELED" -> "취소됨";
            case "FAILED" -> "실패";
            default -> status;
        };
    }

    /**
     * 상태별 예상 일정 계산
     */
    private Map<String, String> calculateSchedule(FundOrderDTO order) {
        Map<String, String> schedule = new HashMap<>();
        
        if (order.getRequestAt() != null) {
            schedule.put("requestDate", order.getRequestAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        }
        
        if (order.getStatus().equals("REQUESTED")) {
            // 금액 확정 예정일: 신청일 다음 영업일
            if (order.getRequestAt() != null) {
                java.time.LocalDate nextBusinessDay = kr.co.bnk.bnk_project.util.HolidayUtil
                        .getNextBusinessDay(order.getRequestAt().toLocalDate());
                schedule.put("expectedFixDate", nextBusinessDay.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            }
        } else if (order.getStatus().equals("FIXED")) {
            if (order.getAmountFixAt() != null) {
                schedule.put("fixDate", order.getAmountFixAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            }
            // 투자 시작 예정일: 확정일 다음 영업일
            if (order.getAmountFixAt() != null) {
                java.time.LocalDate nextBusinessDay = kr.co.bnk.bnk_project.util.HolidayUtil
                        .getNextBusinessDay(order.getAmountFixAt().toLocalDate());
                schedule.put("expectedStartDate", nextBusinessDay.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            }
        } else if (order.getStatus().equals("STARTED")) {
            if (order.getAmountFixAt() != null) {
                schedule.put("fixDate", order.getAmountFixAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            }
            if (order.getStartAt() != null) {
                schedule.put("startDate", order.getStartAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            }
        }
        
        return schedule;
    }

    /**
     * [테스트용] 특정 주문 금액 확정 처리
     * 오늘 가입한 주문도 바로 확정 처리 가능
     * 
     * 사용 예시:
     * POST /api/funds/order/123456789012345/fix
     */
    @PostMapping("/order/{orderId}/fix")
    public ResponseEntity<Map<String, Object>> fixOrder(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            fundOrderFixService.fixOrderById(orderId);
            
            // 처리 후 상태 조회
            FundOrderDTO order = fundOrderMapper.getOrderById(orderId);
            
            response.put("success", true);
            response.put("message", "금액 확정이 완료되었습니다.");
            response.put("orderId", orderId);
            response.put("status", order.getStatus());
            response.put("statusName", getStatusName(order.getStatus()));
            response.put("fixAmount", order.getFixAmount());
            response.put("amountFixAt", order.getAmountFixAt() != null ? 
                    order.getAmountFixAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("주문 금액 확정 실패: orderId={}", orderId, e);
            response.put("success", false);
            response.put("message", "금액 확정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * [테스트용] 특정 주문 투자 시작 처리
     * 금액 확정된 주문을 바로 투자 시작 처리
     * 
     * 사용 예시:
     * POST /api/funds/order/123456789012345/start
     */
    @PostMapping("/order/{orderId}/start")
    public ResponseEntity<Map<String, Object>> startOrder(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            fundOrderStartService.startInvestmentById(orderId);
            
            // 처리 후 상태 조회
            FundOrderDTO order = fundOrderMapper.getOrderById(orderId);
            
            response.put("success", true);
            response.put("message", "투자 시작이 완료되었습니다.");
            response.put("orderId", orderId);
            response.put("status", order.getStatus());
            response.put("statusName", getStatusName(order.getStatus()));
            response.put("startAt", order.getStartAt() != null ? 
                    order.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("주문 투자 시작 실패: orderId={}", orderId, e);
            response.put("success", false);
            response.put("message", "투자 시작 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * [테스트용] 전체 금액 확정 배치 수동 실행
     * 어제 이전에 신청한 모든 REQUESTED 상태 주문을 금액 확정 처리
     * 
     * 자동 배치: 매일 오전 9시에 자동 실행 (FundOrderScheduler)
     * 수동 배치: 테스트 및 확인용으로 언제든지 실행 가능
     * 
     * 사용 예시:
     * POST /api/funds/batch/fix
     */
    @PostMapping("/batch/fix")
    public ResponseEntity<Map<String, Object>> runFixBatch() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("=== 수동 금액 확정 배치 실행 시작 ===");
            fundOrderFixService.fixOrders();
            log.info("=== 수동 금액 확정 배치 실행 완료 ===");
            
            response.put("success", true);
            response.put("message", "금액 확정 배치가 완료되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("수동 금액 확정 배치 실행 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "금액 확정 배치 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * [테스트용] 전체 투자 시작 배치 수동 실행
     * 어제 이전에 확정된 모든 FIXED 상태 주문을 투자 시작 처리
     * 
     * 자동 배치: 매일 오전 9시 30분에 자동 실행 (FundOrderScheduler)
     * 수동 배치: 테스트 및 확인용으로 언제든지 실행 가능
     * 
     * 사용 예시:
     * POST /api/funds/batch/start
     */
    @PostMapping("/batch/start")
    public ResponseEntity<Map<String, Object>> runStartBatch() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("=== 수동 투자 시작 배치 실행 시작 ===");
            fundOrderStartService.startInvestments();
            log.info("=== 수동 투자 시작 배치 실행 완료 ===");
            
            response.put("success", true);
            response.put("message", "투자 시작 배치가 완료되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("수동 투자 시작 배치 실행 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "투자 시작 배치 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

