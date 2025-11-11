package kr.co.bnk.bnk_project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    @GetMapping("/login")
    public String login(){
        return "member/login";
    }

    @GetMapping("/type")
    public String registerType(){
        return "member/registerType";
    }

    @GetMapping("/terms")
    public String terms(){
        return "member/terms";
    }

    @GetMapping("/register")
    public String register(){
        return "member/register";
    }

    @GetMapping("/complete")
    public String complete(){
        return "member/complete";
    }
}
