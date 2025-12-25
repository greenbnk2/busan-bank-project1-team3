package kr.co.bnk.bnk_project.socket;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
    소켓 통신용 입니다.
    이 코드는 배포 할 때만 AP_SERVER_HOST 부분 서버 ip로만 변경하면 됩니다.
*/
@Component
public class TcpClient {

    private static final String AP_SERVER_HOST = "localhost";
    private static final int AP_SERVER_PORT = 9091;

    public String sendRequest(String msg) {
        try (Socket socket = new Socket(AP_SERVER_HOST, AP_SERVER_PORT)) {

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            out.println(msg);
            return in.readLine();

        } catch (Exception e) {
            throw new RuntimeException("AP 서버 통신 오류", e);
        }
    }
}

