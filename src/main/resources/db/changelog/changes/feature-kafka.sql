--liquibase formatted sql

--changeset grigiv:feature-kafka

ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS status text;
COMMENT ON COLUMN tasks.status IS 'Статус задачи';