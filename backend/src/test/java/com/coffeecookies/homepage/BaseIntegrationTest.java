package com.coffeecookies.homepage;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.coffeecookies.homepage.repository.UserRepository;
import com.coffeecookies.homepage.repository.GoldPriceRepository;
import com.coffeecookies.homepage.entity.User;
import com.coffeecookies.homepage.entity.GoldPrice;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 集成测试基类
 * 提供通用的测试设置和数据清理
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected GoldPriceRepository goldPriceRepository;

    @BeforeEach
    void cleanDatabase() {
        goldPriceRepository.deleteAll();
    }

    /**
     * 创建测试用户
     */
    protected User createTestUser(String username, String password, User.Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 在实际测试中应该使用加密后的密码
        user.setRole(role);
        return userRepository.save(user);
    }

    /**
     * 创建测试金价数据
     */
    protected GoldPrice createTestGoldPrice(BigDecimal priceUsd, BigDecimal changeAmount, BigDecimal changePercent) {
        GoldPrice goldPrice = new GoldPrice();
        goldPrice.setPriceUsd(priceUsd);
        goldPrice.setChangeAmount(changeAmount);
        goldPrice.setChangePercent(changePercent);
        goldPrice.setCurrency("USD");
        goldPrice.setRecordedAt(LocalDateTime.now());
        return goldPriceRepository.save(goldPrice);
    }

    /**
     * 将对象转换为JSON字符串
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
