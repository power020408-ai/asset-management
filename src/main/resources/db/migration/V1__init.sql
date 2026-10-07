-- =========================================================
-- V1__init.sql : asset-management 初期スキーマ
-- 対象DB : PostgreSQL
-- このファイルは「適用済み」になったら絶対に書き換えないこと。
-- 変更は V2__xxx.sql として新規追加する。
-- 【桁数の方針】
--   物理的な上限は NUMERIC(38,2) に統一する。
--   業務上の上限（金額は整数13桁まで）はカラム型では表現せず、
--   アプリ層（AssetItemProcessor 等）のバリデーションで検証する。
--   理由：業務ルールの変更にDBマイグレーションを伴わせないため。
--   ※Hibernateの validate は precision/scale を検査しない点に注意。
-- =========================================================

CREATE TABLE asset_master (
                              id         UUID         NOT NULL,
                              asset_id   VARCHAR(255),
                              asset_name VARCHAR(255),
                              asset_type VARCHAR(255),
                              CONSTRAINT pk_asset_master PRIMARY KEY (id),
                              CONSTRAINT uq_asset_master_asset_id UNIQUE (asset_id)
);

CREATE TABLE funds (
                       fund_id     BIGINT        NOT NULL,
                       name        VARCHAR(255),
                       unit_price  NUMERIC(38,2),
                       fund_shares NUMERIC(38,2),
                       nav         NUMERIC(38,2),
                       nav_date    DATE,
                       CONSTRAINT pk_funds PRIMARY KEY (fund_id)
);

CREATE TABLE fund_nav_history (
                                  fund_id     BIGINT        NOT NULL,
                                  nav_date    DATE          NOT NULL,
                                  nav         NUMERIC(38,2),
                                  unit_price  NUMERIC(38,2),
                                  fund_shares NUMERIC(38,2),
                                  created_at  TIMESTAMP(6),
                                  CONSTRAINT pk_fund_nav_history PRIMARY KEY (nav_date, fund_id),
                                  CONSTRAINT fk_fund_nav_history_fund
                                      FOREIGN KEY (fund_id) REFERENCES funds (fund_id)
);

CREATE TABLE assets (
                        fund_id  BIGINT       NOT NULL,
                        asset_id VARCHAR(255) NOT NULL,
                        nav_date DATE         NOT NULL,
                        amount   NUMERIC(38,2),
                        CONSTRAINT pk_assets PRIMARY KEY (nav_date, fund_id, asset_id),
                        CONSTRAINT fk_assets_fund
                            FOREIGN KEY (fund_id) REFERENCES funds (fund_id)
);

CREATE INDEX idx_assets_asset_id ON assets (asset_id);
