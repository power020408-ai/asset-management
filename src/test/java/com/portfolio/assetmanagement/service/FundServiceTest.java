package com.portfolio.assetmanagement.service;

import com.portfolio.assetmanagement.entity.Asset;
import com.portfolio.assetmanagement.entity.Fund;
import com.portfolio.assetmanagement.entity.FundHistoryId;
import com.portfolio.assetmanagement.entity.FundNavHistory;
import com.portfolio.assetmanagement.repository.AssetRepository;
import com.portfolio.assetmanagement.repository.FundNavHistoryRepository;
import com.portfolio.assetmanagement.repository.FundRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundServiceTest {

    //@Mock
    private FundRepository fundRepository;

    //@Mock
    private AssetRepository assetRepository;

    @Mock
    private FundNavHistoryRepository fundNavHistoryRepository;

    @InjectMocks
    private FundService fundService;

    @Test
    void 資産と口数からNAVを計算して保存する() {
        // Arrange：準備
        Long fundId = 1L;
        LocalDate navDate = LocalDate.of(2025, 1, 31);

        Fund fund = mock(Fund.class);
        Asset stock = mock(Asset.class);
        Asset fundShares = mock(Asset.class);
        FundNavHistory history =
                mock(FundNavHistory.class);

        // FundRepositoryの設定
        when(fundRepository.findById(fundId))
                .thenReturn(Optional.of(fund));

        // 通常の資産
        when(stock.getAssetId()).thenReturn("STOCK_001");
        when(stock.getAmount())
                .thenReturn(new BigDecimal("1000000"));

        // ファンドの口数
        when(fundShares.getAssetId()).thenReturn("FUND_SHARES");
        when(fundShares.getAmount())
                .thenReturn(new BigDecimal("80000"));

        // AssetRepositoryの設定
        when(assetRepository.findByFundAndNavDate(fund, navDate))
                .thenReturn(List.of(stock, fundShares));

        // 既存の履歴が見つかるケース
        when(fundNavHistoryRepository.findById(any(FundHistoryId.class)))
                .thenReturn(Optional.of(history));

        // Act：実行
        Fund result = fundService.calculateNav(fundId, navDate);

        // Assert：検証
        assertSame(fund, result);

        // NAVの確認
        // 10,000 × 1,000,000 ÷ 80,000 = 125,000
        verify(history).setNav(new BigDecimal("1000000"));
        verify(history).setUnitPrice(new BigDecimal("125000"));
        verify(history).setFundShares(new BigDecimal("80000"));

        // Fund本体も更新されているか確認
        verify(fund).setNav(new BigDecimal("1000000"));
        verify(fund).setNavDate(navDate);
        verify(fund).setUnitPrice(new BigDecimal("125000"));
        verify(fund).setFundShares(new BigDecimal("80000"));

        // 履歴とFundが保存されたか確認
        verify(fundNavHistoryRepository).save(history);
        verify(fundRepository).save(fund);
    }

    @Test
    void FUND_SHARESの金額がnullなら口数0とみなし基準価額は0になる() {
        // Arrange：準備
        Long fundId = 1L;
        LocalDate navDate = LocalDate.of(2025, 1, 31);

        Fund fund = mock(Fund.class);
        Asset fundSharesNull = mock(Asset.class);
        FundNavHistory history = mock(FundNavHistory.class);

        when(fundRepository.findById(fundId)).thenReturn(Optional.of(fund));

        // 口数の資産だが、金額が null
        when(fundSharesNull.getAssetId()).thenReturn("FUND_SHARES");
        when(fundSharesNull.getAmount()).thenReturn(null);

        when(assetRepository.findByFundAndNavDate(fund, navDate))
                .thenReturn(List.of(fundSharesNull));

        when(fundNavHistoryRepository.findById(any(FundHistoryId.class)))
                .thenReturn(Optional.of(history));

        // Act：実行
        Fund result = fundService.calculateNav(fundId, navDate);

        // Assert：検証
        assertSame(fund, result);

        verify(history).setNav(ZERO);        // 総資産は0
        verify(history).setFundShares(ZERO); // 口数も0（nullは除外）
        verify(history).setUnitPrice(ZERO);  // 0除算を回避して0のまま
       }
}