package kr.co.bnk.bnk_project.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * 공휴일 처리 유틸리티
 * 주말(토요일, 일요일)과 공휴일을 확인하는 기능 제공
 */
public class HolidayUtil {

    // 공휴일 목록 (실제로는 DB나 설정 파일에서 관리하는 것이 좋음)
    private static final Set<LocalDate> HOLIDAYS = new HashSet<>();
    
    static {
        // 2024년 공휴일 예시 (실제로는 DB에서 조회하거나 API로 가져와야 함)
        // 여기서는 예시로 몇 개만 추가
        // 실제 구현 시에는 별도의 공휴일 관리 테이블이나 서비스를 사용해야 함
    }

    /**
     * 해당 날짜가 영업일(평일이고 공휴일이 아닌 날)인지 확인
     * @param date 확인할 날짜
     * @return 영업일이면 true, 주말이거나 공휴일이면 false
     */
    public static boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        // 주말 체크 (토요일, 일요일)
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        
        // 공휴일 체크
        return !HOLIDAYS.contains(date);
    }

    /**
     * 다음 영업일 계산 (주말과 공휴일을 제외한 다음 날)
     * @param date 기준 날짜
     * @return 다음 영업일
     */
    public static LocalDate getNextBusinessDay(LocalDate date) {
        LocalDate nextDay = date.plusDays(1);
        
        while (!isBusinessDay(nextDay)) {
            nextDay = nextDay.plusDays(1);
        }
        
        return nextDay;
    }

    /**
     * N일 후의 영업일 계산
     * @param date 기준 날짜
     * @param days 일수
     * @return N일 후의 영업일
     */
    public static LocalDate getBusinessDayAfter(LocalDate date, int days) {
        LocalDate result = date;
        int remainingDays = days;
        
        while (remainingDays > 0) {
            result = getNextBusinessDay(result);
            remainingDays--;
        }
        
        return result;
    }

    /**
     * 공휴일 추가 (테스트용 또는 동적 추가용)
     */
    public static void addHoliday(LocalDate holiday) {
        HOLIDAYS.add(holiday);
    }

    /**
     * 공휴일 제거
     */
    public static void removeHoliday(LocalDate holiday) {
        HOLIDAYS.remove(holiday);
    }
}

