package com.portfolio.assetmanagement.service;

import com.portfolio.assetmanagement.entity.AssetCsv;
import com.portfolio.assetmanagement.entity.AssetTable;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssetItemProcessorTest {

    private final AssetItemProcessor processor = new AssetItemProcessor();

    @Test
    void 整数部が13桁の金額は正常に変換される() {
        // 境界値：上限ちょうど（9兆9999億...）
        AssetCsv csv = new AssetCsv(
                "1",
                "ASSET001",
                "2026/10/7",
                "9999999999999.99"
        );

        AssetTable result = processor.process(csv);

        assertEquals(new BigDecimal("9999999999999.99"), result.amount());
    }

    @Test
    void 整数部が14桁の金額は拒否する() {
        // 境界値：上限 + 1（10兆）
        AssetCsv csv = new AssetCsv(
                "1",
                "ASSET001",
                "2026/10/7",
                "10000000000000.00"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> processor.process(csv)
        );
    }

    @Test
    void 小数点が3桁以上の金額は拒否する() {
        AssetCsv csv = new AssetCsv(
                "1",
                "ASSET001",
                "2026/10/7",
                "1234.567"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> processor.process(csv)
        );
    }

    @Test
    void 末尾に余分なゼロがついた小数2桁相当は許容する() {
        // 12.300 は実質 12.3 なので許容される
        AssetCsv csv = new AssetCsv(
                "1",
                "ASSET001",
                "2026/10/7",
                "12.300"
        );

        AssetTable result = processor.process(csv);

        // stripTrailingZeros 後の比較、または元の値が保持されていることの確認
        assertEquals(new BigDecimal("12.300"), result.amount());
    }
}