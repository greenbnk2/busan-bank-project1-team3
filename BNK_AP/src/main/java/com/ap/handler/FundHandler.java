package com.ap.handler;

import com.ap.service.FundService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FundHandler implements ApServiceHandler {

    private final ObjectMapper objectMapper;
    private final FundService fundService;

    // 각 serviceName 마다 핸들러 파일 작성해서 action 부분을 구분해줍니다

    @Override
    public String handle(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            String action = node.get("action").asText();

            switch (action) {
                case "getFundMaster":
                    String grade = node.get("investGrade").asText();
                    return objectMapper.writeValueAsString(
                            fundService.getFundMasterByGrade(grade)
                    );

                case "getFundList":
                    return objectMapper.writeValueAsString(
                            fundService.getFundList()
                    );

                case "getProductDetail":
                    String fundCode = node.get("fundCode").asText();
                    return objectMapper.writeValueAsString(
                            fundService.getProductDetail(fundCode)
                    );
                default:
                    return "{ \"error\": \"Unknown action: " + action + "\" }";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "{ \"error\": \"FundHandler error\" }";
        }
    }
}
