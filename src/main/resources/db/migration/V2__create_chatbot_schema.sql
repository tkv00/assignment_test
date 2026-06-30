CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) NOT NULL,
    password_hash TEXT NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'member',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_email_not_blank CHECK (length(btrim(email)) > 0),
    CONSTRAINT chk_users_password_hash_not_blank CHECK (length(btrim(password_hash)) > 0),
    CONSTRAINT chk_users_name_not_blank CHECK (length(btrim(name)) > 0),
    CONSTRAINT chk_users_role CHECK (role IN ('member', 'admin'))
);

CREATE INDEX idx_users_created_at ON users (created_at);

CREATE TABLE chat_threads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_chatted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_chat_threads_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT uq_chat_threads_id_user UNIQUE (id, user_id)
);

CREATE INDEX idx_chat_threads_user_created_at ON chat_threads (user_id, created_at);
CREATE INDEX idx_chat_threads_user_last_chatted_at ON chat_threads (user_id, last_chatted_at DESC);

CREATE TABLE chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    thread_id UUID NOT NULL,
    user_id UUID NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    model VARCHAR(100),
    provider VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_chats_thread_user
        FOREIGN KEY (thread_id, user_id)
        REFERENCES chat_threads (id, user_id),
    CONSTRAINT fk_chats_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT chk_chats_question_not_blank CHECK (length(btrim(question)) > 0),
    CONSTRAINT chk_chats_answer_not_blank CHECK (length(btrim(answer)) > 0),
    CONSTRAINT chk_chats_provider_not_blank CHECK (length(btrim(provider)) > 0)
);

CREATE INDEX idx_chats_user_created_at ON chats (user_id, created_at);
CREATE INDEX idx_chats_thread_created_at ON chats (thread_id, created_at);
CREATE INDEX idx_chats_created_at ON chats (created_at);

CREATE TABLE feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    chat_id UUID NOT NULL,
    positive BOOLEAN NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_feedback_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT fk_feedback_chat
        FOREIGN KEY (chat_id)
        REFERENCES chats (id),
    CONSTRAINT uq_feedback_user_chat UNIQUE (user_id, chat_id),
    CONSTRAINT chk_feedback_status CHECK (status IN ('pending', 'resolved'))
);

CREATE INDEX idx_feedback_user_created_at ON feedback (user_id, created_at);
CREATE INDEX idx_feedback_positive_created_at ON feedback (positive, created_at);
CREATE INDEX idx_feedback_status_created_at ON feedback (status, created_at);
CREATE INDEX idx_feedback_created_at ON feedback (created_at);

CREATE TABLE activity_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    activity_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_activity_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT chk_activity_logs_type CHECK (activity_type IN ('signup', 'login', 'chat_created'))
);

CREATE INDEX idx_activity_logs_type_created_at ON activity_logs (activity_type, created_at);
CREATE INDEX idx_activity_logs_created_at ON activity_logs (created_at);
