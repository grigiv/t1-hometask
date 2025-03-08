--liquibase formatted sql

--changeset grigiv:init

CREATE TABLE IF NOT EXISTS tasks
(
    task_id     bigserial NOT NULL,
    title       text      NOT NULL,
    description text      NULL,
    user_id     bigint    NULL
);

ALTER TABLE tasks
    ADD CONSTRAINT users_pkey PRIMARY KEY (task_id);

COMMENT ON TABLE tasks IS 'Задачи';
COMMENT ON COLUMN tasks.task_id IS 'Идентификатор задачи';
COMMENT ON COLUMN tasks.title IS 'Название задачи';
COMMENT ON COLUMN tasks.description IS 'Описание задачи';
COMMENT ON COLUMN tasks.user_id IS 'Идентификатор пользователя';