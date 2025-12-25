package kr.co.bnk.bnk_project.service.mobile;

import kr.co.bnk.bnk_project.dto.mobile.MockAccountDTO;
import kr.co.bnk.bnk_project.mapper.mobile.MockAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MockInvestmentService {

    private final MockAccountMapper mockAccountMapper;

    public MockAccountDTO getAccountByCustNo(Long custNo) {
        return mockAccountMapper.findByCustNo(custNo);
    }

    public String getRiskType(Long custNo) {
        return mockAccountMapper.findRiskTypeByCustNo(custNo);
    }

    public boolean createAccount(MockAccountDTO dto) {
        // 간단한 모의 계좌번호 생성 (예: MOCK-순번 또는 UUID 활용)
        String newAcctNo = "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        dto.setMockAcctNo(newAcctNo);

        return mockAccountMapper.insertMockAccount(dto) > 0;
    }
}