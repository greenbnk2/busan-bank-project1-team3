package kr.co.bnk.bnk_project.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import kr.co.bnk.bnk_project.service.admin.EditLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 세션 만료 시 편집 잠금을 자동으로 해제하는 리스너
 */
@Component
@WebListener
public class EditLockSessionListener implements HttpSessionListener {

    private static EditLockService editLockService;

    @Autowired
    public void setEditLockService(EditLockService editLockService) {
        EditLockSessionListener.editLockService = editLockService;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        if (editLockService != null) {
            String sessionId = se.getSession().getId();
            // 세션 만료 시 해당 세션의 모든 잠금 해제
            editLockService.unlockAllBySession(sessionId);
        }
    }
}

