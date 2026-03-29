package com.coffeecookies.homepage.controller;

import com.coffeecookies.homepage.dto.*;
import com.coffeecookies.homepage.service.GoldPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gold-price")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GoldPriceController {

    private final GoldPriceService goldPriceService;

    @GetMapping("/current")
    public ResponseEntity<Result<GoldPriceDTO>> getCurrentPrice(
            @RequestParam(defaultValue = "USD") String currency) {
        return ResponseEntity.ok(Result.success(goldPriceService.getCurrentPrice(currency)));
    }

    @GetMapping("/history")
    public ResponseEntity<Result<List<GoldPriceDTO.PricePoint>>> getPriceHistory(
            @RequestParam(defaultValue = "USD") String currency,
            @RequestParam(defaultValue = "30") int days) {
        // Limit days to valid range
        if (days < 1) days = 1;
        if (days > 365) days = 365;
        return ResponseEntity.ok(Result.success(goldPriceService.getPriceHistory(currency, days)));
    }

    @GetMapping("/currencies")
    public ResponseEntity<Result<List<CurrencyDTO>>> getSupportedCurrencies() {
        return ResponseEntity.ok(Result.success(goldPriceService.getSupportedCurrencies()));
    }
}
