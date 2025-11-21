package kr.co.bnk.bnk_project.controller.admin.settings;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin/dev")
public class AdminLogController {

    private final String LOG_FILE_PATH = "logs/BNK_PROJECT.log";

    @GetMapping("/logs")
    public String viewServerLogs(Model model) {
        // "오늘 날짜"에 해당하는 로그만 전부 읽어오기
        List<String> logLines = readTodayLogs(LOG_FILE_PATH);

        model.addAttribute("logs", logLines);
        model.addAttribute("activeMenu", "dev-logs");

        return "admin/settings/logViewer";
    }

    // 오늘 날짜의 로그만 뒤에서부터 읽어오는 메서드
    private List<String> readTodayLogs(String filePath) {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            lines.add("❌ 로그 파일이 없습니다: " + file.getAbsolutePath());
            return lines;
        }

        // 오늘 날짜 문자열 구하기 (예: "2025-11-20")
        String todayStr = LocalDate.now().toString();
        // 올해 연도 (예: "2025-") - 날짜가 바뀐 것을 감지하기 위함
        String currentYear = String.valueOf(LocalDate.now().getYear()) + "-";

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long fileLength = raf.length();
            long pointer = fileLength - 1;
            StringBuilder sb = new StringBuilder();

            // 파일 끝에서부터 한 글자씩 앞으로 이동하며 읽기
            while (pointer >= 0) {
                raf.seek(pointer);
                int c = raf.read();

                if (c == '\n') {
                    // 한 줄이 완성되었을 때
                    if (sb.length() > 0) {
                        String line = new String(sb.reverse().toString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

                        // 1. 이 줄이 날짜로 시작하는지 확인 (예: 2025-11-20...)
                        if (line.startsWith(currentYear)) {
                            // 2. 오늘 날짜인지 확인
                            if (line.startsWith(todayStr)) {
                                lines.add(line); // 오늘 로그면 담기
                            } else {
                                // 3. 날짜가 있는데 오늘이 아니라면 (어제 로그 도달) -> 여기서 중단!
                                break;
                            }
                        } else {
                            // 4. 날짜가 없는 줄(스택 트레이스, 에러 상세 내용 등)은 그냥 담기
                            // (보통 로그 바로 밑에 달린 내용이므로 오늘 로그의 일부로 간주)
                            lines.add(line);
                        }
                        sb.setLength(0); // 버퍼 초기화
                    }
                } else if (c != '\r') {
                    sb.append((char) c);
                }
                pointer--;
            }

            // 파일의 맨 첫 줄 처리
            if (sb.length() > 0) {
                String line = new String(sb.reverse().toString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                if (line.startsWith(todayStr) || !line.startsWith(currentYear)) {
                    lines.add(line);
                }
            }

        } catch (IOException e) {
            lines.add("❌ 로그 읽기 실패: " + e.getMessage());
        }

        // 뒤에서부터 읽었으므로 순서가 [최신 -> 과거]입니다.
        // 보기 편하게 [과거 -> 최신]으로 뒤집어 줍니다.
        Collections.reverse(lines);

        return lines;
    }
}