package com.portfolio.assetmanagement.service;

import com.portfolio.assetmanagement.entity.Asset;
import com.portfolio.assetmanagement.entity.Fund;

import com.portfolio.assetmanagement.entity.FundHistoryId;
import com.portfolio.assetmanagement.entity.FundNavHistory;
import com.portfolio.assetmanagement.repository.AssetRepository;
import com.portfolio.assetmanagement.repository.FundRepository;
import com.portfolio.assetmanagement.repository.FundNavHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FundService 単体テスト")
class FundServiceTest {

    @Mock
    FundRepository fundRepository;

    @Mock
    AssetRepository assetRepository;

    @Mock
    FundNavHistoryRepository fundNavHistoryRepository;

    @InjectMocks
    FundService fundService;

    @Nested
    @DisplayName("基準価額・純資産総額の計算")
    class NavCalculationTests {

        @Test
        @DisplayName("正常系: 割り切れる場合、純資産総額と基準価額が正しく計算される")


        void 正常系_割り切れる場合() {
            // Given: 総口数 800,000口、保有銘柄のNAV合計 1,000,000円
            // Arrange：準備
            Long fundId = 1L;
            LocalDate navDate = LocalDate.of(2025, 1, 31);

            Fund fund = mock(Fund.class);
            Asset asset = mock(Asset.class);
            FundNavHistory history =
                    mock(FundNavHistory.class);

            // FundRepositoryの設定
            when(fundRepository.findById(fundId))
                    .thenReturn(Optional.of(fund));

            // ファンドの口数
            when(fund.getFundShares())
                    .thenReturn(new BigDecimal("800000"));

            // 通常の資産
            when(asset.getAmount())
                    .thenReturn(new BigDecimal("1000000"));

            // AssetRepositoryの設定
            when(assetRepository.findByFundAndNavDate(fund, navDate))
                    .thenReturn(List.of(asset));

            // 既存の履歴が見つかるケース
            when(fundNavHistoryRepository.findById(any(FundHistoryId.class)))
                    .thenReturn(Optional.of(history));

            // Act：実行
            Fund result = fundService.calculateNav(fundId, navDate);

            // Assert：検証
            assertSame(fund, result);

            // NAVの確認
            // 10,000 × 1,000,000 ÷ 800,000 = 12,500
            ArgumentCaptor<BigDecimal> navCaptor = ArgumentCaptor.forClass(BigDecimal.class);
            verify(history).setNav(navCaptor.capture());
            assertThat(navCaptor.getValue()).isEqualByComparingTo("1000000");   // 値で比較

            verify(history).setUnitPrice(new BigDecimal("12500"));
            verify(history).setFundShares(new BigDecimal("800000"));

            // Fund本体も更新されているか確認
            //verify(fund).setNav(new BigDecimal("1000000"));
            navCaptor = ArgumentCaptor.forClass(BigDecimal.class);
            verify(fund).setNav(navCaptor.capture());
            assertThat(navCaptor.getValue()).isEqualByComparingTo("1000000");   // 値で比較

            verify(fund).setNavDate(navDate);
            verify(fund).setUnitPrice(new BigDecimal("12500"));

            // 履歴とFundが保存されたか確認
            verify(fundNavHistoryRepository).save(history);
            verify(fundRepository).save(fund);
        }

        @Test
        @DisplayName("端数処理: 割り切れない場合、基準価額は小数点以下切り捨て（DOWN）になる")
        void 端数処理_小数点以下切り捨て() {
            Long fundId = 1L;
            LocalDate navDate = LocalDate.of(2025, 1, 31);

            Fund fund = mock(Fund.class);
            Asset asset = mock(Asset.class);
            FundNavHistory history = mock(FundNavHistory.class);

            when(fundRepository.findById(fundId)).thenReturn(Optional.of(fund));
            when(fund.getFundShares()).thenReturn(new BigDecimal("240000"));
            when(asset.getAmount()).thenReturn(new BigDecimal("1000000"));
            when(assetRepository.findByFundAndNavDate(fund, navDate))
                    .thenReturn(List.of(asset));
            when(fundNavHistoryRepository.findById(any(FundHistoryId.class)))
                    .thenReturn(Optional.of(history));

            fundService.calculateNav(fundId, navDate);
            System.out.println();
            // 10,000 × 1,000,000 ÷ 240,000 = 41,666.6666...
            // DOWN なら 41,666 / HALF_UP なら 41,667
            verify(history).setUnitPrice(new BigDecimal("41666"));
        }
    }

    @Nested
    @DisplayName("境界値・異常系")
    class BoundaryTests {

        @Test
        @DisplayName("ゼロ除算回避: 総口数が 0 の場合は基準価額 0 を返す")
        void ゼロ除算回避_総口数ゼロ() {
            Long fundId = 1L;
            LocalDate navDate = LocalDate.of(2025, 1, 31);
            Fund fund = mock(Fund.class);
            Asset asset = mock(Asset.class);
            FundNavHistory history = mock(FundNavHistory.class);

            when(fundRepository.findById(fundId)).thenReturn(Optional.of(fund));
            when(fund.getFundShares()).thenReturn(BigDecimal.ZERO);   // ← 0口
            when(asset.getAmount()).thenReturn(new BigDecimal("1000000"));
            when(assetRepository.findByFundAndNavDate(fund, navDate))
                    .thenReturn(List.of(asset));
            when(fundNavHistoryRepository.findById(any(FundHistoryId.class)))
                    .thenReturn(Optional.of(history));

            fundService.calculateNav(fundId, navDate);

            ArgumentCaptor<BigDecimal> navCaptor = ArgumentCaptor.forClass(BigDecimal.class);
            verify(history).setNav(navCaptor.capture());
            assertThat(navCaptor.getValue()).isEqualByComparingTo("1000000");   // 値で比較

            verify(history).setUnitPrice(BigDecimal.ZERO);       // 0除算せず0のまま
        }

        @Test
        @DisplayName("空データ: 資産が 0 件の場合は純資産・基準価額ともに 0 を返す")
        void 空データ_資産ゼロ件() {
            Long fundId = 1L;
            LocalDate navDate = LocalDate.of(2025, 1, 31);
            Fund fund = mock(Fund.class);
            FundNavHistory history = mock(FundNavHistory.class);

            when(fundRepository.findById(fundId)).thenReturn(Optional.of(fund));
            when(fund.getFundShares()).thenReturn(new BigDecimal("800000"));
            when(assetRepository.findByFundAndNavDate(fund, navDate))
                    .thenReturn(List.of());   // ← 0件
            when(fundNavHistoryRepository.findById(any(FundHistoryId.class)))
                    .thenReturn(Optional.of(history));

            fundService.calculateNav(fundId, navDate);

            ArgumentCaptor<BigDecimal> navCaptor = ArgumentCaptor.forClass(BigDecimal.class);
            verify(history).setNav(navCaptor.capture());
            assertThat(navCaptor.getValue()).isEqualByComparingTo("0");   // 値で比較

            verify(history).setUnitPrice(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("nullガード: 金額が null の資産は無視して合算する")
        void nullガード_金額がnullの資産は無視() {
            Long fundId = 1L;
            LocalDate navDate = LocalDate.of(2025, 1, 31);
            Fund fund = mock(Fund.class);
            FundNavHistory history = mock(FundNavHistory.class);

            Asset a1 = mock(Asset.class);
            Asset a2 = mock(Asset.class);   // getAmount() は null を返す（未スタブ）
            Asset a3 = mock(Asset.class);

            when(fundRepository.findById(fundId)).thenReturn(Optional.of(fund));
            when(fund.getFundShares()).thenReturn(new BigDecimal("800000"));
            when(a1.getAmount()).thenReturn(new BigDecimal("500000"));
            when(a3.getAmount()).thenReturn(new BigDecimal("500000"));
            when(assetRepository.findByFundAndNavDate(fund, navDate))
                    .thenReturn(List.of(a1, a2, a3));
            when(fundNavHistoryRepository.findById(any(FundHistoryId.class)))
                    .thenReturn(Optional.of(history));

            fundService.calculateNav(fundId, navDate);

            // null を飛ばして 500,000 + 500,000 = 1,000,000
            ArgumentCaptor<BigDecimal> navCaptor = ArgumentCaptor.forClass(BigDecimal.class);
            verify(history).setNav(navCaptor.capture());
            assertThat(navCaptor.getValue()).isEqualByComparingTo("1000000");   // 値で比較

            verify(history).setUnitPrice(new BigDecimal("12500"));
        }

        @Test
        @DisplayName("ファンド不在: 見つからなければ例外を投げ、保存しない")
        void ファンド不在_例外を投げる() {
            Long fundId = 999L;
            LocalDate navDate = LocalDate.of(2025, 1, 31);
            when(fundRepository.findById(fundId)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> {fundService.calculateNav(fundId, navDate);});

            verify(fundRepository, never()).save(any(Fund.class));
        }

        @Test
        @DisplayName("履歴なし: 既存が無ければ新しい履歴を作って保存する")
        void 履歴なし_新規作成される() {
            Long fundId = 1L;
            LocalDate navDate = LocalDate.of(2025, 1, 31);
            Fund fund = mock(Fund.class);
            Asset asset = mock(Asset.class);

            when(fundRepository.findById(fundId)).thenReturn(Optional.of(fund));
            when(fund.getFundShares()).thenReturn(new BigDecimal("800000"));
            when(asset.getAmount()).thenReturn(new BigDecimal("1000000"));
            when(assetRepository.findByFundAndNavDate(fund, navDate))
                    .thenReturn(List.of(asset));
            when(fundNavHistoryRepository.findById(any(FundHistoryId.class)))
                    .thenReturn(Optional.empty());   // ← 履歴なし

            fundService.calculateNav(fundId, navDate);

            ArgumentCaptor<FundNavHistory> captor =
                    ArgumentCaptor.forClass(FundNavHistory.class);
            verify(fundNavHistoryRepository).save(captor.capture());

            FundNavHistory saved = captor.getValue();
            assertThat(saved.getFund()).isSameAs(fund);          // ファンドが紐づく
            assertThat(saved.getNavDate()).isEqualTo(navDate);   // 基準日が入る
            assertThat(saved.getUnitPrice()).isEqualByComparingTo("12500");

        }
    }
}