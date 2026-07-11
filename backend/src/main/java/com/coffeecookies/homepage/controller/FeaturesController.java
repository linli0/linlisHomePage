package com.coffeecookies.homepage.controller;

import com.coffeecookies.homepage.config.FeatureFlagsProperties;
import com.coffeecookies.homepage.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class FeaturesController {

    private final FeatureFlagsProperties featureFlags;

    @GetMapping
    public ResponseEntity<Result<Map<String, Boolean>>> getFeatures() {
        Map<String, Boolean> flags = new LinkedHashMap<>();
        flags.put("ai", featureFlags.getAi().isEnabled());
        flags.put("tweets", featureFlags.getTweets().isEnabled());
        flags.put("quant", featureFlags.getQuant().isEnabled());
        flags.put("xiaomi", featureFlags.getXiaomi().isEnabled());
        return ResponseEntity.ok(Result.success(flags));
    }
}
