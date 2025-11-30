package kr.co.bnk.bnk_project.config;

import kr.co.bnk.bnk_project.dto.admin.AdminFundMasterDTO;
import kr.co.bnk.bnk_project.dto.admin.FundMasterRevisionDTO;
import kr.co.bnk.bnk_project.mapper.admin.AdminFundMapper;
import kr.co.bnk.bnk_project.mapper.admin.FundMasterRevisionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
     * 1. Job 정의 - 예약된 펀드를 운용중으로 바꾸는 Job
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
                if (iterator == null) {
                    List<AdminFundMasterDTO> list = adminFundMapper.selectFundsForOperateReserve();
                    iterator = list.iterator();
                }
                return iterator.hasNext() ? iterator.next() : null;
            }
        };
    }

    /**
     * 4. Processor - 상태 값만 변경 (실제 DB 반영은 Writer에서)
     */
    @Bean
    public ItemProcessor<AdminFundMasterDTO, AdminFundMasterDTO> fundOperateReserveProcessor() {
        return fund -> {
            fund.setOperStatus("운용중");
            fund.setReserveYn("N");
            return fund;
        };
    }

    /**
     * 5. Writer - DB에 UPDATE 처리
     */
    @Bean
    public ItemWriter<AdminFundMasterDTO> fundOperateReserveWriter() {
        return items -> {
            for (AdminFundMasterDTO fund : items) {
                adminFundMapper.updateOperStatusToRunning(
                        fund.getFundCode(),
                        "batch_system"
                );
            }
        };
    }

    /**
     * 6. 스케줄러에서 Job 실행 (매 1분마다 실행)
     */
    @Scheduled(cron = "0 * * * * *")
    public void runFundOperateReserveJob() {
        try {
            Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("fundOperateReserveJob");
            if (runningExecutions != null && !runningExecutions.isEmpty()) {
                return;
            }

            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(fundOperateReserveJob(), params);
        } catch (Exception e) {
            // 에러 발생 시 무시
        }
    }

    // 새로운 배치 Job 추가 - revision 적용 배치
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

                    if (list != null && !list.isEmpty()) {
                        list.removeIf(rev -> rev == null || rev.getRevId() == null);
                    }

                    if (list == null || list.isEmpty()) {
                        return null;
                    }

                    iterator = list.iterator();
                }

                if (iterator.hasNext()) {
                    FundMasterRevisionDTO revision = iterator.next();
                    
                    if (revision == null || revision.getRevId() == null) {
                        return null;
                    }
                    
                    return revision;
                }
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
            for (FundMasterRevisionDTO revision : items) {
                try {
                    if (revision == null || revision.getRevId() == null) {
                        continue;
                    }
                    
                    Long revId = revision.getRevId();
                    
                    fundMasterRevisionMapper.applyRevisionToMaster(revId);
                    fundMasterRevisionMapper.updateRevisionStatusToApplied(revId);
                    adminFundMapper.clearReserveTime(revision.getFundCode());
                } catch (Exception e) {
                    throw e;
                }
            }
        };
    }

    @Scheduled(cron = "0 * * * * *")
    public void runApplyRevisionJob() {
        try {
            Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("applyRevisionJob");
            if (runningExecutions != null && !runningExecutions.isEmpty()) {
                return;
            }

            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(applyRevisionJob(), params);
        } catch (Exception e) {
            // 에러 발생 시 무시
        }
    }
}
