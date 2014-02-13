CREATE USER IF NOT EXISTS "" SALT '' HASH '' ADMIN;

--

CREATE TABLE "commit_review_tasks"(
    "commit_id" VARCHAR NOT NULL,
    "user_id" VARCHAR NOT NULL,
    "created_date" TIMESTAMP NOT NULL
);
ALTER TABLE "commit_review_tasks" ADD CONSTRAINT "commit_review_tasks_id" PRIMARY KEY("commit_id", "user_id");

CREATE TABLE "events"(
    "id" VARCHAR NOT NULL,
    "event_date" TIMESTAMP NOT NULL,
    "event_type" VARCHAR NOT NULL,
    "originating_user_id" VARCHAR NOT NULL
);
ALTER TABLE "events" ADD CONSTRAINT "events_id" PRIMARY KEY("id");

CREATE TABLE "invitations"(
    "code" VARCHAR NOT NULL,
    "invitation_sender" VARCHAR NOT NULL,
    "expiry_date" TIMESTAMP NOT NULL
);
ALTER TABLE "invitations" ADD CONSTRAINT "invitations_id" PRIMARY KEY("code");

CREATE TABLE "repository_statuses"(
    "repository_name" VARCHAR NOT NULL,
    "head_id" VARCHAR,
    "ready" BOOLEAN NOT NULL,
    "error" VARCHAR
);
ALTER TABLE "repository_statuses" ADD CONSTRAINT "repository_statuses_id" PRIMARY KEY("repository_name");

CREATE TABLE "heartbeats"(
    "user_id" VARCHAR NOT NULL,
    "last_heartbeat" TIMESTAMP NOT NULL
);
ALTER TABLE "heartbeats" ADD CONSTRAINT "heartbeats_id" PRIMARY KEY("user_id");

-- FOLLOWUPS, REACTIONS

CREATE TABLE "comments"(
    "id" VARCHAR NOT NULL,
    "commit_id" VARCHAR NOT NULL,
    "author_id" VARCHAR NOT NULL,
    "posting_time" TIMESTAMP NOT NULL,
    "message" VARCHAR NOT NULL,
    "file_name" VARCHAR,
    "line_number" INTEGER
);
ALTER TABLE "comments" ADD CONSTRAINT "comments_id" PRIMARY KEY("id");

CREATE TABLE "likes"(
    "id" VARCHAR NOT NULL,
    "commit_id" VARCHAR NOT NULL,
    "author_id" VARCHAR NOT NULL,
    "posting_time" TIMESTAMP NOT NULL,
    "file_name" VARCHAR,
    "line_number" INTEGER
);
ALTER TABLE "likes" ADD CONSTRAINT "likes_id" PRIMARY KEY("id");

CREATE TABLE "followups"(
    "id" VARCHAR NOT NULL,
    "receiving_user_id" VARCHAR NOT NULL,
    "thread_commit_id" VARCHAR NOT NULL,
    "thread_file_name" VARCHAR,
    "thread_line_number" INTEGER,
    "last_reaction_id" VARCHAR NOT NULL,
    "last_reaction_created_date" TIMESTAMP NOT NULL,
    "last_reaction_author" VARCHAR NOT NULL
);
ALTER TABLE "followups" ADD CONSTRAINT "followups_id" PRIMARY KEY("id");

CREATE INDEX "followups_search" ON "followups"("receiving_user_id", "thread_commit_id", "thread_file_name", "thread_line_number");

CREATE TABLE "followups_reactions"(
    "followup_id" VARCHAR NOT NULL,
    "reaction_id" VARCHAR NOT NULL
);
ALTER TABLE "followups_reactions" ADD CONSTRAINT "followup_reactions_id" PRIMARY KEY("followup_id", "reaction_id");

ALTER TABLE "followups_reactions" ADD CONSTRAINT "followup_reactions_fk" FOREIGN KEY("followup_id") REFERENCES "followups"("id") ON DELETE CASCADE ON UPDATE CASCADE NOCHECK;

-- COMMITS

CREATE TABLE "commit_infos"(
    "id" VARCHAR NOT NULL,
    "sha" VARCHAR NOT NULL,
    "message" VARCHAR NOT NULL,
    "author_name" VARCHAR NOT NULL,
    "author_email" VARCHAR NOT NULL,
    "commiter_name" VARCHAR NOT NULL,
    "commiter_email" VARCHAR NOT NULL,
    "author_date" TIMESTAMP NOT NULL,
    "commit_date" TIMESTAMP NOT NULL
);
ALTER TABLE "commit_infos" ADD CONSTRAINT "commit_infos_id" PRIMARY KEY("id");

CREATE INDEX "commit_infos_dates" ON "commit_infos"("commit_date", "author_date");

CREATE TABLE "commit_infos_files"(
    "commit_info_id" VARCHAR NOT NULL,
    "filename" VARCHAR NOT NULL,
    "status" VARCHAR NOT NULL,
    "patch" VARCHAR NOT NULL
);

CREATE TABLE "commit_infos_parents"(
    "commit_info_id" VARCHAR NOT NULL,
    "parent" VARCHAR NOT NULL
);

ALTER TABLE "commit_infos_parents" ADD CONSTRAINT "commit_info_parents_fk" FOREIGN KEY("commit_info_id") REFERENCES "commit_infos"("id") NOCHECK;
ALTER TABLE "commit_infos_files" ADD CONSTRAINT "commit_info_files_fk" FOREIGN KEY("commit_info_id") REFERENCES "commit_infos"("id") NOCHECK;

-- USERS

CREATE TABLE "users_last_notifs"(
    "user_id" VARCHAR NOT NULL,
    "last_commits_dispatch" TIMESTAMP,
    "last_followups_dispatch" TIMESTAMP
);
ALTER TABLE "users_last_notifs" ADD CONSTRAINT "user_last_notifs_id" PRIMARY KEY("user_id");

CREATE TABLE "users_settings"(
    "user_id" VARCHAR NOT NULL,
    "avatar_url" VARCHAR NOT NULL,
    "email_notif" BOOLEAN NOT NULL,
    "email_daily_updates" BOOLEAN NOT NULL,
    "app_tour_done" BOOLEAN NOT NULL
);
ALTER TABLE "users_settings" ADD CONSTRAINT "user_settings_id" PRIMARY KEY("user_id");

CREATE TABLE "users_authentications"(
    "user_id" VARCHAR NOT NULL,
    "provider" VARCHAR NOT NULL,
    "username" VARCHAR NOT NULL,
    "username_lowercase" VARCHAR NOT NULL,
    "token" VARCHAR NOT NULL,
    "salt" VARCHAR NOT NULL
);
ALTER TABLE "users_authentications" ADD CONSTRAINT "user_authentications_id" PRIMARY KEY("user_id");

CREATE TABLE "users"(
    "id" VARCHAR NOT NULL,
    "name" VARCHAR NOT NULL,
    "email_lowercase" VARCHAR NOT NULL,
    "token" VARCHAR NOT NULL,
    "regular" BOOLEAN NOT NULL
);
ALTER TABLE "users" ADD CONSTRAINT "users_id" PRIMARY KEY("id");

ALTER TABLE "users" ADD CONSTRAINT "last_notifs_fk" FOREIGN KEY("id") REFERENCES "users_last_notifs"("user_id") ON DELETE CASCADE ON UPDATE CASCADE NOCHECK;
ALTER TABLE "users" ADD CONSTRAINT "settings_fk" FOREIGN KEY("id") REFERENCES "users_settings"("user_id") ON DELETE CASCADE ON UPDATE CASCADE NOCHECK;
ALTER TABLE "users" ADD CONSTRAINT "auth_fk" FOREIGN KEY("id") REFERENCES "users_authentications"("user_id") ON DELETE CASCADE ON UPDATE CASCADE NOCHECK;