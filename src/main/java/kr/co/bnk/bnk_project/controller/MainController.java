package kr.co.bnk.bnk_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class MainController {

    @GetMapping({"/","/index"})
    public String index(){
        return "index";
    }

    /**
     * JavaScript 세션 타이머가 '연장' 버튼을 누를 때 호출할 API
     * 이 API가 호출되는 것만으로도 서버의 세션 시간이 20분으로 다시 초기화됩니다.
     * @return 간단한 JSON 응답
     */

    @PostMapping("/api/session/extend")
    @ResponseBody // HTML 템플릿이 아닌 JSON/텍스트 데이터를 직접 반환
    public Map<String, String> extendSession() {
        // 이 메서드가 호출되면 세션이 갱신.
        return Map.of("status", "ok", "message", "Session extended");
    }
}
