package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.dto.CurrencyDTO;
import com.coffeecookies.homepage.dto.GoldPriceDTO;
import com.coffeecookies.homepage.entity.GoldPrice;
import com.coffeecookies.homepage.repository.ExchangeRateRepository;
import com.coffeecookies.homepage.repository.GoldPriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoldPriceServiceConfigurationTest {

    @Mock
    private GoldPriceRepository goldPriceRepository;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    private GoldPriceService goldPriceService;

    @BeforeEach
    void setUp() {
        goldPriceService = new GoldPriceService(goldPriceRepository, exchangeRateRepository);
        ReflectionTestUtils.setField(goldPriceService, "metalpriceApiEnabled", false);
    }

    @Test
    void getCurrentPrice_shouldCreateInitialPrice_whenNoPriceExists() {
        when(goldPriceRepository.findTopByOrderByRecordedAtDesc()).thenReturn(Optional.empty());
        when(goldPriceRepository.save(any(GoldPrice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(goldPriceRepository.findByRecordedAtAfterOrderByRecordedAtAsc(any())).thenReturn(List.of());

        GoldPriceDTO dto = goldPriceService.getCurrentPrice("USD");

        assertThat(dto).isNotNull();
        assertThat(dto.getCurrency()).isEqualTo("USD");
        assertThat(dto.getPrice()).isEqualByComparingTo("2050.00");
        verify(goldPriceRepository).save(any(GoldPrice.class));
    }

    @Test
    void getPriceHistory_shouldGenerateHistory_whenRepositoryReturnsEmpty() {
        when(goldPriceRepository.findByRecordedAtAfterOrderByRecordedAtAsc(any())).thenReturn(List.of());
        when(goldPriceRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<GoldPriceDTO.PricePoint> points = goldPriceService.getPriceHistory("USD", 5);

        assertThat(points).hasSize(6);
        assertThat(points).allMatch(point -> point.getPrice() != null);
    }

    @Test
    void getSupportedCurrencies_shouldReturnAllConfiguredCurrencies() {
        when(exchangeRateRepository.findByFromCurrencyAndToCurrency(any(), any())).thenReturn(Optional.empty());
        List<CurrencyDTO> currencies = new ArrayList<>(goldPriceService.getSupportedCurrencies());

        assertThat(currencies).hasSize(4);
        assertThat(currencies).extracting(CurrencyDTO::getCode)
                .containsExactlyInAnyOrder("USD", "CNY", "EUR", "GBP");
    }

    @Test
    void updateGoldPrice_shouldSkipWhenMetalPriceApiDisabled() {
        goldPriceService.updateGoldPrice();

        verifyNoInteractions(goldPriceRepository);
    }

    @Test
    void getCurrentPrice_shouldApplyFallbackExchangeRate() {
        GoldPrice price = new GoldPrice();
        price.setPriceUsd(new BigDecimal("100.00"));
        price.setChangeAmount(BigDecimal.ZERO);
        price.setChangePercent(BigDecimal.ZERO);
        price.setCurrency("USD");
        price.setRecordedAt(LocalDateTime.now());

        when(goldPriceRepository.findTopByOrderByRecordedAtDesc()).thenReturn(Optional.of(price));
        when(goldPriceRepository.findByRecordedAtAfterOrderByRecordedAtAsc(any())).thenReturn(List.of(price));
        when(exchangeRateRepository.findByFromCurrencyAndToCurrency(any(), any())).thenReturn(Optional.empty());

        GoldPriceDTO cnyDto = goldPriceService.getCurrentPrice("CNY");

        assertThat(cnyDto.getPrice()).isEqualByComparingTo("720.00");
    }
}
