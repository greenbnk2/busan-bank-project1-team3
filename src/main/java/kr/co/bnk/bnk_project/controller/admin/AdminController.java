package kr.co.bnk.bnk_project.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping({"/", "/main"})
    public String adminMain() {
        return "admin/adminMain";
    }
}

