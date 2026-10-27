CREATE TABLE IF NOT EXISTS communities (
 id uuid PRIMARY KEY, name text NOT NULL, bank text NOT NULL DEFAULT '', account text NOT NULL DEFAULT '', holder text NOT NULL DEFAULT ''
);
CREATE TABLE IF NOT EXISTS residents (
 id uuid PRIMARY KEY, community_id uuid NOT NULL REFERENCES communities(id), name text NOT NULL,
 phone text UNIQUE, address text NOT NULL, role text NOT NULL CHECK(role IN ('admin','resident')),
 pin_hash text, active boolean NOT NULL DEFAULT true, demo boolean NOT NULL DEFAULT false,
 created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS sessions (
 token_hash text PRIMARY KEY, resident_id uuid NOT NULL REFERENCES residents(id), expires_at timestamptz NOT NULL
);
CREATE TABLE IF NOT EXISTS auth_limits (
 key text PRIMARY KEY, count int NOT NULL, window_start timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS invoices (
 id uuid PRIMARY KEY, community_id uuid NOT NULL REFERENCES communities(id), resident_id uuid NOT NULL REFERENCES residents(id),
 period text NOT NULL, title text NOT NULL, amount bigint NOT NULL CHECK(amount>0), due_date date NOT NULL,
 status text NOT NULL DEFAULT 'unpaid' CHECK(status IN ('unpaid','pending','paid')), reference text NOT NULL DEFAULT '',
 paid_at timestamptz, UNIQUE(resident_id,period,title)
);
CREATE TABLE IF NOT EXISTS news (
 id uuid PRIMARY KEY, community_id uuid NOT NULL REFERENCES communities(id), title text NOT NULL, body text NOT NULL,
 category text NOT NULL, author_id uuid NOT NULL REFERENCES residents(id), created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS expenses (
 id uuid PRIMARY KEY, community_id uuid NOT NULL REFERENCES communities(id), title text NOT NULL, amount bigint NOT NULL CHECK(amount>0),
 date date NOT NULL, author_id uuid NOT NULL REFERENCES residents(id)
);
CREATE TABLE IF NOT EXISTS reports (
 id uuid PRIMARY KEY, community_id uuid NOT NULL REFERENCES communities(id), resident_id uuid NOT NULL REFERENCES residents(id),
 title text NOT NULL, body text NOT NULL, status text NOT NULL DEFAULT 'open' CHECK(status IN ('open','resolved')),
 created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS audit_logs (
 id bigserial PRIMARY KEY, actor_id uuid, action text NOT NULL, entity_id text, created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS deletion_requests (
 id uuid PRIMARY KEY, resident_id uuid NOT NULL REFERENCES residents(id), requested_at timestamptz NOT NULL DEFAULT now(),
 UNIQUE(resident_id)
);
CREATE INDEX IF NOT EXISTS invoices_community ON invoices(community_id, resident_id);
CREATE INDEX IF NOT EXISTS news_community ON news(community_id, created_at DESC);
CREATE INDEX IF NOT EXISTS reports_community ON reports(community_id, created_at DESC);
