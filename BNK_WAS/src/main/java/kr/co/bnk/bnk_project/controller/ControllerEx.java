package kr.co.bnk.bnk_project.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.co.bnk.bnk_project.socket.TcpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ControllerEx {

    private final ObjectMapper objectMapper;
    private final TcpClient tcpClient;

/*
    예시용 펀드 상세 입니다.
    requestJson에 service는 대분류 action은 동작 그뒤는 추가 사항들 입니다.
    ex) 만약 펀드 리스트를 출력하려고 하면
    ㄴ> serviceName = fund , action = getFundList(이건 작업하시는 분이 이름 지정하면됩니다.)
    각 컨트롤러마다 requestJson만 수정하고 밑에 통신 부분은 복붙 하시면 됩니다.
*/
    @GetMapping("/productDetail/{fundCode}")
    public String productDetail(@PathVariable("fundCode") String fundCode, Model model) {

        String requestJson = "{ \"serviceName\": \"fund\", \"action\": \"getProductDetail\", \"fundCode\": \"" + fundCode + "\" }";

        String responseJson = tcpClient.sendRequest(requestJson);

        try {
            Map<String, Object> detailMap = objectMapper.readValue(
                    responseJson,
                    new TypeReference<Map<String, Object>>() {}
            );

            model.addAttribute("detail", detailMap);

        } catch (Exception e) {
            System.err.println("JSON 파싱 오류: " + e.getMessage());
            model.addAttribute("detail", Map.of("fundName", "데이터 로드 실패"));
        }

        return "productDetail";
    }
}

