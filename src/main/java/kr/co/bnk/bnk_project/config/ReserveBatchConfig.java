package kr.co.bnk.bnk_project.config;

import kr.co.bnk.bnk_project.dto.admin.AdminFundMasterDTO;
import kr.co.bnk.bnk_project.dto.admin.FundMasterRevisionDTO;
import kr.co.bnk.bnk_project.mapper.admin.AdminFundMapper;
import kr.co.bnk.bnk_project.mapper.admin.FundMasterRevisionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ReserveBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final AdminFundMapper adminFundMapper;
    private final FundMasterRevisionMapper fundMasterRevisionMapper;

    /**
     * 1. Job 정의 - 예약된 펀드를 운용상태로 변경하는 Job
     */
    @Bean
    public Job fundOperateReserveJob() {
        return new JobBuilder("fundOperateReserveJob", jobRepository)
                .start(fundOperateReserveStep())
                .build();
    }

    /**
     * 2. Step 정의
     */
    @Bean
    public Step fundOperateReserveStep() {
        return new StepBuilder("fundOperateReserveStep", jobRepository)
                .<AdminFundMasterDTO, AdminFundMasterDTO>chunk(100, transactionManager)
                .reader(fundOperateReserveReader())
                .processor(fundOperateReserveProcessor())
                .writer(fundOperateReserveWriter())
                .build();
    }

    /**
     * 3. Reader - 예약 대상 펀드 조회
     */
    @Bean
    public ItemReader<AdminFundMasterDTO> fundOperateReserveReader() {
        return new ItemReader<>() {
            private Iterator<AdminFundMasterDTO> iterator;

            @Override
            public AdminFundMasterDTO read() {
                if (iterator == null || !iterator.hasNext()) {
                    log.info("[fundOperateReserveReader] ===== 데이터 조회 시작 =====");
                    log.info("[fundOperateReserveReader] selectFundsForOperateReserve() 호출 전");
                    try {
                        List<AdminFundMasterDTO> list = adminFundMapper.selectFundsForOperateReserve();
                        log.info("[fundOperateReserveReader] selectFundsForOperateReserve() 호출 완료");
                        int count = list != null ? list.size() : 0;
                        log.info("[fundOperateReserveReader] 예약 대상 펀드 조회 결과: {}건", count);

                        if (list != null && !list.isEmpty()) {
                            log.info("[fundOperateReserveReader] 조회된 펀드 목록:");
                            for (AdminFundMasterDTO fund : list) {
                                log.info("[fundOperateReserveReader]   - FUND_CODE={}, OPER_STATUS={}, OPER_START_AT={}",
                                        fund.getFundCode(), fund.getOperStatus(), fund.getOperStartAt());
                            }
                        }

                        if (list == null || list.isEmpty()) {
                            log.info("[fundOperateReserveReader] 조회 결과 없음 - Reader 종료 예정");
                            iterator = null;
                            return null;
                        }

                        iterator = list.iterator();
                        log.info("[fundOperateReserveReader] ===== 데이터 조회 완료 =====");
                    } catch (Exception e) {
                        log.error("[fundOperateReserveReader] 데이터 조회 중 오류 발생", e);
                        throw e;
                    }
                }

                if (iterator != null && iterator.hasNext()) {
                    AdminFundMasterDTO fund = iterator.next();
                    log.info("[fundOperateReserveReader] 펀드 읽기: FUND_CODE={}, OPER_STATUS={}",
                            fund.getFundCode(), fund.getOperStatus());
                    return fund;
                }

                log.info("[fundOperateReserveReader] 읽을 데이터 없음 - 조회 완료");
                return null;
            }
        };
    }

    /**
     * 4. Processor - 검증/필터링만 수행 (상태 변경은 Writer에서 처리)
     */
    @Bean
    public ItemProcessor<AdminFundMasterDTO, AdminFundMasterDTO> fundOperateReserveProcessor() {
        return fund -> {
            if (fund == null || fund.getFundCode() == null) {
                log.warn("[fundOperateReserveProcessor] 유효하지 않은 펀드 데이터 스킵");
                return null;
            }
            // 여기서는 상태를 변경하지 않고, 그대로 Writer로 전달만 한다.
            return fund;
        };
    }

    /**
     * 5. Writer - DB에 UPDATE 처리
     */
    @Bean
    public ItemWriter<AdminFundMasterDTO> fundOperateReserveWriter() {
        return items -> {
            log.info("[fundOperateReserveWriter] 처리 시작: {}건", items.size());
            for (AdminFundMasterDTO fund : items) {
                try {
                    if (fund == null || fund.getFundCode() == null) {
                        log.warn("[fundOperateReserveWriter] 유효하지 않은 펀드 데이터 스킵");
                        continue;
                    }

                    String fundCode = fund.getFundCode();
                    String operStatus = fund.getOperStatus(); // Reader에서 조회된 '원래 상태' 기준

                    // 등록완료 상태인 경우: 운용대기로 변경
                    if ("등록완료".equals(operStatus)) {
                        log.info("[fundOperateReserveWriter] 등록완료 → 운용대기: FUND_CODE={}", fundCode);
                        adminFundMapper.updateStatusToPending(fundCode);
                        adminFundMapper.clearReserveTime(fundCode);
                    }
                    // 운용대기 상태인 경우: 운용중으로 변경
                    else if ("운용대기".equals(operStatus)) {
                        log.info("[fundOperateReserveWriter] 운용대기 → 운용중: FUND_CODE={}", fundCode);
                        adminFundMapper.updateOperStatusToRunning(
                                fundCode,
                                "batch_system"
                        );
                    } else {
                        log.info("[fundOperateReserveWriter] 상태 변경 대상 아님: FUND_CODE={}, OPER_STATUS={}",
                                fundCode, operStatus);
                    }
                } catch (Exception e) {
                    log.error("[fundOperateReserveWriter] 펀드 처리 중 오류 발생: FUND_CODE={}",
                            fund != null ? fund.getFundCode() : "null", e);
                    throw e;
                }
            }
            log.info("[fundOperateReserveWriter] 처리 완료");
        };
    }

    /**
     * 6. 스케줄러에서 Job 실행 (매 1분마다 실행)
     */
    @Scheduled(cron = "0 * * * * *")
    public void runFundOperateReserveJob() {
        log.info("[runFundOperateReserveJob] 스케줄러 실행 시작");
        try {
            Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("fundOperateReserveJob");
            if (runningExecutions != null && !runningExecutions.isEmpty()) {
                log.info("[runFundOperateReserveJob] 실행 중인 Job 확인: {}건", runningExecutions.size());
                LocalDateTime currentDateTime = LocalDateTime.now();
                long timeoutMs = 5 * 60 * 1000; // 5분
                boolean hasRunningJob = false;

                for (JobExecution execution : runningExecutions) {
                    if (execution.isRunning()) {
                        LocalDateTime startDateTime = execution.getStartTime();
                        if (startDateTime != null) {
                            long elapsedTime = java.time.Duration.between(startDateTime, currentDateTime).toMillis();
                            if (elapsedTime > timeoutMs) {
                                log.warn("[runFundOperateReserveJob] 타임아웃된 작업 감지: executionId={}, elapsed={}ms",
                                        execution.getId(), elapsedTime);
                                // 타임아웃된 작업은 무시하고 새 Job 실행 계속 진행
                            } else {
                                log.info("[runFundOperateReserveJob] 아직 진행 중인 Job 존재: executionId={}, elapsed={}ms",
                                        execution.getId(), elapsedTime);
                                hasRunningJob = true;
                            }
                        } else {
                            // startTime이 없는 이상한 경우는 실행 중으로 간주
                            hasRunningJob = true;
                        }
                    }
                }

                if (hasRunningJob) {
                    log.info("[runFundOperateReserveJob] 실행 중인 Job이 있어 스킵");
                    return;
                }
            }

            // Reader에서 알아서 대상이 없으면 바로 종료되므로 여기서는 Job만 실행
            log.info("[runFundOperateReserveJob] fundOperateReserveJob 실행 시작");
            Job job = fundOperateReserveJob();
            org.springframework.batch.core.JobParameters params =
                    new org.springframework.batch.core.JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters();

            jobLauncher.run(job, params);
            log.info("[runFundOperateReserveJob] fundOperateReserveJob 실행 완료");
        } catch (Exception e) {
            log.error("[runFundOperateReserveJob] Job 실행 중 오류 발생", e);
        }
    }

    // ==========================
    //  Revision 적용 배치 Job
    // ==========================

    @Bean
    public Job applyRevisionJob() {
        return new JobBuilder("applyRevisionJob", jobRepository)
                .start(applyRevisionStep())
                .build();
    }

    @Bean
    public Step applyRevisionStep() {
        return new StepBuilder("applyRevisionStep", jobRepository)
                .<FundMasterRevisionDTO, FundMasterRevisionDTO>chunk(100, transactionManager)
                .reader(applyRevisionReader())
                .processor(applyRevisionProcessor())
                .writer(applyRevisionWriter())
                .build();
    }

    @Bean
    public ItemReader<FundMasterRevisionDTO> applyRevisionReader() {
        return new ItemReader<>() {
            private Iterator<FundMasterRevisionDTO> iterator;

            @Override
            public FundMasterRevisionDTO read() {
                if (iterator == null || !iterator.hasNext()) {
                    List<FundMasterRevisionDTO> list = fundMasterRevisionMapper.selectRevisionsToApply();
                    log.info("[applyRevisionReader] 적용 대상 revision 조회: {}건", list != null ? list.size() : 0);

                    if (list != null && !list.isEmpty()) {
                        int beforeSize = list.size();
                        list.removeIf(rev -> rev == null || rev.getRevId() == null);
                        if (beforeSize != list.size()) {
                            log.warn("[applyRevisionReader] 유효하지 않은 revision 제거: {}건 → {}건", beforeSize, list.size());
                        }
                    }

                    if (list == null || list.isEmpty()) {
                        log.debug("[applyRevisionReader] 적용 대상 revision 없음");
                        iterator = null;
                        return null;
                    }

                    iterator = list.iterator();
                }

                if (iterator.hasNext()) {
                    FundMasterRevisionDTO revision = iterator.next();

                    if (revision == null || revision.getRevId() == null) {
                        log.warn("[applyRevisionReader] 유효하지 않은 revision 데이터 스킵");
                        return null;
                    }

                    log.debug("[applyRevisionReader] revision 읽기: REV_ID={}, FUND_CODE={}",
                            revision.getRevId(), revision.getFundCode());
                    return revision;
                }
                log.debug("[applyRevisionReader] 읽을 데이터 없음");
                return null;
            }
        };
    }

    @Bean
    public ItemProcessor<FundMasterRevisionDTO, FundMasterRevisionDTO> applyRevisionProcessor() {
        return revision -> {
            if (revision == null || revision.getRevId() == null) {
                return null;
            }
            return revision;
        };
    }

    @Bean
    public ItemWriter<FundMasterRevisionDTO> applyRevisionWriter() {
        return items -> {
            log.info("[applyRevisionWriter] 처리 시작: {}건", items.size());
            for (FundMasterRevisionDTO revision : items) {
                try {
                    if (revision == null || revision.getRevId() == null) {
                        log.warn("[applyRevisionWriter] 유효하지 않은 revision 데이터 스킵");
                        continue;
                    }

                    Long revId = revision.getRevId();
                    String fundCode = revision.getFundCode();

                    log.info("[applyRevisionWriter] revision 적용 시작: REV_ID={}, FUND_CODE={}", revId, fundCode);

                    // revision 내용을 FUND_MASTER에 업데이트
                    fundMasterRevisionMapper.applyRevisionToMaster(revId);
                    log.debug("[applyRevisionWriter] FUND_MASTER 업데이트 완료: REV_ID={}", revId);

                    // revision 상태를 '적용완료'로 변경
                    fundMasterRevisionMapper.updateRevisionStatusToApplied(revId);
                    log.debug("[applyRevisionWriter] revision 상태 변경 완료: REV_ID={}", revId);

                    // 예약 시간 초기화
                    adminFundMapper.clearReserveTime(fundCode);
                    log.debug("[applyRevisionWriter] 예약 시간 초기화 완료: FUND_CODE={}", fundCode);

                    // 반영예약 완료 후 revision 삭제
                    fundMasterRevisionMapper.deleteRevision(revId);
                    log.info("[applyRevisionWriter] revision 적용 및 삭제 완료: REV_ID={}, FUND_CODE={}", revId, fundCode);
                } catch (Exception e) {
                    log.error("[applyRevisionWriter] revision 처리 중 오류 발생: REV_ID={}",
                            revision != null ? revision.getRevId() : "null", e);
                    throw e;
                }
            }
            log.info("[applyRevisionWriter] 처리 완료");
        };
    }

    @Scheduled(cron = "0 * * * * *")
    public void runApplyRevisionJob() {
        log.info("[runApplyRevisionJob] 스케줄러 실행 시작");
        try {
            Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("applyRevisionJob");
            if (runningExecutions != null && !runningExecutions.isEmpty()) {
                log.info("[runApplyRevisionJob] 실행 중인 Job 확인: {}건", runningExecutions.size());
                LocalDateTime currentDateTime = LocalDateTime.now();
                long timeoutMs = 5 * 60 * 1000; // 5분

                for (JobExecution execution : runningExecutions) {
                    if (execution.isRunning()) {
                        LocalDateTime startDateTime = execution.getStartTime();
                        if (startDateTime != null) {
                            long elapsedTime = java.time.Duration.between(startDateTime, currentDateTime).toMillis();
                            if (elapsedTime > timeoutMs) {
                                log.warn("[runApplyRevisionJob] 타임아웃된 작업 감지: executionId={}, elapsed={}ms",
                                        execution.getId(), elapsedTime);
                                // 타임아웃된 작업은 무시하고 계속 진행
                            } else {
                                log.info("[runApplyRevisionJob] 실행 중인 Job이 있어 스킵: executionId={}, elapsed={}ms",
                                        execution.getId(), elapsedTime);
                                return;
                            }
                        } else {
                            // startTime이 없으면 실행 중으로 간주
                            log.info("[runApplyRevisionJob] 실행 중인 Job이 있어 스킵: executionId={}", execution.getId());
                            return;
                        }
                    }
                }
            }

            org.springframework.batch.core.JobParameters params =
                    new org.springframework.batch.core.JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters();

            log.info("[runApplyRevisionJob] Job 실행 시작");
            jobLauncher.run(applyRevisionJob(), params);
            log.info("[runApplyRevisionJob] Job 실행 완료");
        } catch (Exception e) {
            log.error("[runApplyRevisionJob] Job 실행 중 오류 발생", e);
        }
    }

    // ==========================
    //  적용완료된 revision 정리 배치 Job
    // ==========================

    @Bean
    public Job cleanupAppliedRevisionJob() {
        return new JobBuilder("cleanupAppliedRevisionJob", jobRepository)
                .start(cleanupAppliedRevisionStep())
                .build();
    }

    @Bean
    public Step cleanupAppliedRevisionStep() {
        return new StepBuilder("cleanupAppliedRevisionStep", jobRepository)
                .<FundMasterRevisionDTO, FundMasterRevisionDTO>chunk(100, transactionManager)
                .reader(cleanupAppliedRevisionReader())
                .processor(cleanupAppliedRevisionProcessor())
                .writer(cleanupAppliedRevisionWriter())
                .build();
    }

    @Bean
    public ItemReader<FundMasterRevisionDTO> cleanupAppliedRevisionReader() {
        return new ItemReader<>() {
            private Iterator<FundMasterRevisionDTO> iterator;

            @Override
            public FundMasterRevisionDTO read() {
                if (iterator == null || !iterator.hasNext()) {
                    List<FundMasterRevisionDTO> list = fundMasterRevisionMapper.selectAppliedRevisions();
                    log.info("[cleanupAppliedRevisionReader] 적용완료된 revision 조회: {}건", list != null ? list.size() : 0);

                    if (list != null && !list.isEmpty()) {
                        int beforeSize = list.size();
                        list.removeIf(rev -> rev == null || rev.getRevId() == null);
                        if (beforeSize != list.size()) {
                            log.warn("[cleanupAppliedRevisionReader] 유효하지 않은 revision 제거: {}건 → {}건", beforeSize, list.size());
                        }
                    }

                    if (list == null || list.isEmpty()) {
                        log.debug("[cleanupAppliedRevisionReader] 정리 대상 revision 없음");
                        iterator = null;
                        return null;
                    }

                    iterator = list.iterator();
                }

                if (iterator.hasNext()) {
                    FundMasterRevisionDTO revision = iterator.next();

                    if (revision == null || revision.getRevId() == null) {
                        log.warn("[cleanupAppliedRevisionReader] 유효하지 않은 revision 데이터 스킵");
                        return null;
                    }

                    log.debug("[cleanupAppliedRevisionReader] revision 읽기: REV_ID={}", revision.getRevId());
                    return revision;
                }
                log.debug("[cleanupAppliedRevisionReader] 읽을 데이터 없음");
                return null;
            }
        };
    }

    @Bean
    public ItemProcessor<FundMasterRevisionDTO, FundMasterRevisionDTO> cleanupAppliedRevisionProcessor() {
        return revision -> {
            if (revision == null || revision.getRevId() == null) {
                return null;
            }
            return revision;
        };
    }

    @Bean
    public ItemWriter<FundMasterRevisionDTO> cleanupAppliedRevisionWriter() {
        return items -> {
            log.info("[cleanupAppliedRevisionWriter] 처리 시작: {}건", items.size());
            for (FundMasterRevisionDTO revision : items) {
                try {
                    if (revision == null || revision.getRevId() == null) {
                        log.warn("[cleanupAppliedRevisionWriter] 유효하지 않은 revision 데이터 스킵");
                        continue;
                    }

                    Long revId = revision.getRevId();
                    log.info("[cleanupAppliedRevisionWriter] 적용완료된 revision 삭제: REV_ID={}", revId);
                    // 적용완료 상태인 revision 삭제
                    fundMasterRevisionMapper.deleteRevision(revId);
                    log.debug("[cleanupAppliedRevisionWriter] revision 삭제 완료: REV_ID={}", revId);
                } catch (Exception e) {
                    log.error("[cleanupAppliedRevisionWriter] revision 삭제 중 오류 발생: REV_ID={}",
                            revision != null ? revision.getRevId() : "null", e);
                    throw e;
                }
            }
            log.info("[cleanupAppliedRevisionWriter] 처리 완료");
        };
    }

    @Scheduled(cron = "0 0 * * * *") // 매 시간마다 실행
    public void runCleanupAppliedRevisionJob() {
        log.info("[runCleanupAppliedRevisionJob] 스케줄러 실행 시작");
        try {
            Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("cleanupAppliedRevisionJob");
            if (runningExecutions != null && !runningExecutions.isEmpty()) {
                log.info("[runCleanupAppliedRevisionJob] 실행 중인 Job이 있어 스킵");
                return;
            }

            org.springframework.batch.core.JobParameters params =
                    new org.springframework.batch.core.JobParametersBuilder()
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters();

            log.info("[runCleanupAppliedRevisionJob] Job 실행 시작");
            jobLauncher.run(cleanupAppliedRevisionJob(), params);
            log.info("[runCleanupAppliedRevisionJob] Job 실행 완료");
        } catch (Exception e) {
            log.error("[runCleanupAppliedRevisionJob] Job 실행 중 오류 발생(무시)", e);
            // 에러 발생 시 무시
        }
    }
}
