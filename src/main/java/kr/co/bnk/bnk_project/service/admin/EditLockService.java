package kr.co.bnk.bnk_project.service.admin;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 펀드 수정 페이지 동시 편집 방지를 위한 세션 기반 잠금 서비스
 */
@Service
public class EditLockService {

    // 잠금 정보를 저장하는 Map (펀드 코드 -> 잠금 정보)
    private final Map<String, LockInfo> editLocks = new ConcurrentHashMap<>();

    // 잠금 만료 시간 (30분)
    private static final long LOCK_EXPIRY_MINUTES = 30;

    /**
     * 잠금 정보를 담는 내부 클래스
     */
    public static class LockInfo {
        private final String sessionId;
        private final String userId;
        private final LocalDateTime lockTime;

        public LockInfo(String sessionId, String userId) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.lockTime = LocalDateTime.now();
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getUserId() {
            return userId;
        }

        public LocalDateTime getLockTime() {
            return lockTime;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(lockTime.plusMinutes(LOCK_EXPIRY_MINUTES));
        }
    }

    /**
     * 잠금 시도
     * @param fundCode 펀드 코드
     * @param sessionId 세션 ID
     * @param userId 사용자 ID
     * @return null이면 성공, null이 아니면 잠금한 사용자 이름 반환 (실패)
     */
    public String tryLock(String fundCode, String sessionId, String userId) {
        if (fundCode == null || sessionId == null || userId == null) {
            return "잠금 정보가 올바르지 않습니다.";
        }

        LockInfo existing = editLocks.get(fundCode);

        // 기존 잠금이 있고, 만료되지 않았으며, 다른 세션이 잠금한 경우
        if (existing != null && !existing.isExpired()) {
            if (!existing.getSessionId().equals(sessionId)) {
                return existing.getUserId(); // 다른 사용자가 잠금 중
            }
            // 같은 세션이면 잠금 갱신
        }

        // 잠금 설정 또는 갱신
        editLocks.put(fundCode, new LockInfo(sessionId, userId));
        return null; // 성공
    }

    /**
     * 잠금 상태 확인
     * @param fundCode 펀드 코드
     * @param sessionId 세션 ID
     * @return null이면 잠금 없음 또는 본인 잠금, null이 아니면 잠금한 사용자 이름
     */
    public String checkLock(String fundCode, String sessionId) {
        if (fundCode == null || sessionId == null) {
            return null;
        }

        LockInfo lock = editLocks.get(fundCode);
        if (lock == null) {
            return null; // 잠금 없음
        }

        // 만료된 잠금은 무시
        if (lock.isExpired()) {
            editLocks.remove(fundCode);
            return null;
        }

        // 같은 세션이면 null 반환 (본인 잠금)
        if (lock.getSessionId().equals(sessionId)) {
            return null;
        }

        // 다른 세션이 잠금 중
        return lock.getUserId();
    }

    /**
     * 잠금 해제
     * @param fundCode 펀드 코드
     * @param sessionId 세션 ID
     * @return true면 해제 성공, false면 실패 (다른 사용자 잠금)
     */
    public boolean unlock(String fundCode, String sessionId) {
        if (fundCode == null || sessionId == null) {
            return false;
        }

        LockInfo lock = editLocks.get(fundCode);
        if (lock == null) {
            return true; // 잠금이 없으면 성공으로 간주
        }

        // 같은 세션이 아니면 해제 불가
        if (!lock.getSessionId().equals(sessionId)) {
            return false;
        }

        editLocks.remove(fundCode);
        return true;
    }

    /**
     * 잠금 갱신 (30분 자동 해제 방지)
     * @param fundCode 펀드 코드
     * @param sessionId 세션 ID
     * @return true면 갱신 성공, false면 실패
     */
    public boolean keepLock(String fundCode, String sessionId) {
        if (fundCode == null || sessionId == null) {
            return false;
        }

        LockInfo lock = editLocks.get(fundCode);
        if (lock == null) {
            return false; // 잠금이 없음
        }

        // 같은 세션이 아니면 갱신 불가
        if (!lock.getSessionId().equals(sessionId)) {
            return false;
        }

        // 잠금 시간 갱신 (새로운 LockInfo 생성)
        editLocks.put(fundCode, new LockInfo(sessionId, lock.getUserId()));
        return true;
    }

    /**
     * 특정 세션의 모든 잠금 해제 (세션 만료 시 사용)
     * @param sessionId 세션 ID
     */
    public void unlockAllBySession(String sessionId) {
        if (sessionId == null) {
            return;
        }

        editLocks.entrySet().removeIf(entry -> 
            entry.getValue().getSessionId().equals(sessionId)
        );
    }
}

