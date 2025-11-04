-- V1__init_schema.sql
-- 最小可用的四张表：item / item_batch / oauth_token / sync_log
-- 设计目标：SKU 主数据 + 批次/效期库存 + OAuth 管理 + 同步日志
-- Postgres 在 Flyway 下默认整段事务执行（失败即回滚）

-- 1) 商品主数据（SKU）
CREATE TABLE IF NOT EXISTS item (
  id                  BIGSERIAL PRIMARY KEY,
  qb_item_id          TEXT UNIQUE,                 -- QuickBooks 外部ID，用于幂等/upsert
  name                TEXT        NOT NULL,        -- 商品名
  sku                 TEXT,                        -- 内部SKU（可空）
  barcode             TEXT UNIQUE,                 -- 条码（可空，但若填则唯一）
  unit                TEXT,                        -- 计量单位（如 box/bottle）
  is_active           BOOLEAN     NOT NULL DEFAULT TRUE, -- 是否启用/上架
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 可选的查询辅助：按名称、条码、外部ID检索
CREATE INDEX IF NOT EXISTS idx_item_name ON item (name);
CREATE INDEX IF NOT EXISTS idx_item_qb_item_id ON item (qb_item_id);
-- barcode 已有 UNIQUE 约束，不再单独建索引

-- 2) 批次/效期库存（同一SKU的不同有效期分开管理）
CREATE TABLE IF NOT EXISTS item_batch (
  id                  BIGSERIAL PRIMARY KEY,
  item_id             BIGINT      NOT NULL REFERENCES item(id) ON DELETE CASCADE,
  batch_code          TEXT,                       -- 批号/生产批（有则填）
  expiration_date     DATE        NOT NULL,       -- 有效期（到期日）
  quantity            INTEGER     NOT NULL DEFAULT 0,  -- 当前库存数量
  location            TEXT,                       -- 仓位/库区（可选）
  received_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- 入库时间
  note                TEXT,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  -- 防止同一SKU在同一批次/同一效期重复建记录（允许 NULL 批号并存）
  CONSTRAINT uq_item_batch UNIQUE (item_id, expiration_date, batch_code)
);

-- 到期/查询常用索引
CREATE INDEX IF NOT EXISTS idx_item_batch_item_id ON item_batch (item_id);
CREATE INDEX IF NOT EXISTS idx_item_batch_expiration ON item_batch (expiration_date);
CREATE INDEX IF NOT EXISTS idx_item_batch_item_exp ON item_batch (item_id, expiration_date);

-- 3) QuickBooks OAuth 管理（集中存 access/refresh token）
CREATE TABLE IF NOT EXISTS oauth_token (
  id                         BIGSERIAL PRIMARY KEY,
  provider                   TEXT        NOT NULL DEFAULT 'quickbooks',   -- 预留多供应商
  realm_id                   TEXT        NOT NULL,                        -- QBO 公司标识
  token_type                 TEXT,                                        -- bearer 等
  scope                      TEXT,                                        -- 授权范围
  access_token               TEXT        NOT NULL,
  refresh_token              TEXT        NOT NULL,
  expires_at                 TIMESTAMPTZ NOT NULL,                        -- access_token 过期时间
  refresh_token_expires_at   TIMESTAMPTZ,                                 -- 可选
  last_refreshed_at          TIMESTAMPTZ,                                 -- 最近刷新时间
  created_at                 TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at                 TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_oauth_realm_provider UNIQUE (realm_id, provider)
);

-- 4) 同步日志（排障与重试依据）
CREATE TABLE IF NOT EXISTS sync_log (
  id            BIGSERIAL PRIMARY KEY,
  job_type      TEXT        NOT NULL,   -- 例如：ITEM_PULL / INVENTORY_PULL / INVOICE_PUSH
  status        TEXT        NOT NULL,   -- SUCCESS / FAIL / RETRY
  started_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  finished_at   TIMESTAMPTZ,
  duration_ms   INTEGER,                -- 可选：finished-started 计算后填入
  message       TEXT,                   -- 简要说明
  details       JSONB,                  -- 结构化错误/返回包（可选）
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 常用查询索引（按时间/类型/状态）
CREATE INDEX IF NOT EXISTS idx_sync_log_started_at ON sync_log (started_at);
CREATE INDEX IF NOT EXISTS idx_sync_log_job_type ON sync_log (job_type);
CREATE INDEX IF NOT EXISTS idx_sync_log_status ON sync_log (status);