package com.ap.socket;


import com.ap.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

// 이 파일은 수정 X

@Component
@RequiredArgsConstructor
public class SocketServer {

    // 비즈니스 로직(Service)
    private final BusinessService businessService;

    @PostConstruct
    public void startServer() {
        // 소켓 서버는 별도 스레드에서 실행
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(9091)) {
                System.out.println("AP Socket Server Started... (port: 9091)");

                while (true) {
                    // WAS 연결 기다림
                    Socket socket = serverSocket.accept();
                    System.out.println("WAS Connected!");

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream())
                    );
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    // WAS → AP JSON 요청
                    String request = in.readLine();
                    System.out.println("Received from WAS: " + request);

                    // 비즈니스 로직 호출 (DB 포함)
                    String response = businessService.route(request);

                    // AP → WAS 응답
                    out.println(response);
                    System.out.println("Sent to WAS: " + response);

                    socket.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

