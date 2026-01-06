package kr.co.bnk.bnk_project.controller;

import kr.co.bnk.bnk_project.dto.LoginRequestDTO;
import kr.co.bnk.bnk_project.dto.LoginResponseDTO;
import kr.co.bnk.bnk_project.dto.SignupRequestDTO;
import kr.co.bnk.bnk_project.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO request) {

        System.out.println("🔥🔥🔥 로그인 API 호출됨 🔥🔥🔥");
        System.out.println("username = " + request.getUsername());

        String token = authService.login(
                request.getUsername(),
                request.getPassword()
        );

        System.out.println("🔥🔥🔥 로그인 처리 완료 🔥🔥🔥");

        return ResponseEntity.ok(new LoginResponseDTO(token));

    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @RequestBody SignupRequestDTO request) {

        System.out.println("🔥 회원가입 컨트롤러 진입");
        System.out.println("custId = " + request.getCustId());

        authService.signup(request);

        return ResponseEntity.ok().build();
    }
}

