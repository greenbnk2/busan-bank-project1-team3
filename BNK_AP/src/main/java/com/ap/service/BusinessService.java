package com.ap.service;

import com.ap.handler.ApServiceHandler;
import com.ap.handler.FundHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final ObjectMapper objectMapper;
    private final FundHandler fundHandler;
    private Map<String, ApServiceHandler> handlerMap;

    @PostConstruct
    public void init() {
        handlerMap = new HashMap<>();
        handlerMap.put("fund", fundHandler); // 이런 형식으로 서비스 추가
    }

    public String route(String requestJson) {
        try {
            JsonNode node = objectMapper.readTree(requestJson);

            String serviceName = node.get("serviceName").asText();
            ApServiceHandler handler = handlerMap.get(serviceName);

            if (handler == null) {
                return "{ \"error\": \"Unknown service: " + serviceName + "\" }";
            }

            return handler.handle(requestJson);

        } catch (Exception e) {
            return "{ \"error\": \"Invalid JSON\" }";
        }
    }
}