package com.example.demo.service;

import com.example.demo.dto.Currency;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService {
    private final RestTemplate restTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Value("${currency-change.url}")
    private String url;

    public CurrencyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, BigDecimal> getCurrency() {

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                List<Currency> currencyList = objectMapper.readValue(response.getBody(), new TypeReference<List<Currency>>() {
                });
                return convertListToMap(currencyList);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private Map<String, BigDecimal> convertListToMap(List<Currency> currencies) {
        Map<String, BigDecimal> values = new HashMap<>();
        currencies.stream().map(el -> values.put(el.symbol(), el.price())).count();
        return values;
    }
}
