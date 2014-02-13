;
CREATE USER IF NOT EXISTS "" SALT '' HASH '' ADMIN;
CREATE CACHED TABLE PUBLIC."comments"(
    "id" VARCHAR NOT NULL,
    "commit_id" VARCHAR NOT NULL,
    "author_id" VARCHAR NOT NULL,
    "posting_time" TIMESTAMP NOT NULL,
    "message" VARCHAR NOT NULL,
    "file_name" VARCHAR,
    "line_number" INTEGER
);
ALTER TABLE PUBLIC."comments" ADD CONSTRAINT PUBLIC.CONSTRAINT_D PRIMARY KEY("id");
-- 0 +/- SELECT COUNT(*) FROM PUBLIC."comments";
CREATE CACHED TABLE PUBLIC."commit_infos"(
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
ALTER TABLE PUBLIC."commit_infos" ADD CONSTRAINT PUBLIC.CONSTRAINT_E PRIMARY KEY("id");
-- 1649 +/- SELECT COUNT(*) FROM PUBLIC."commit_infos";
CREATE CACHED TABLE PUBLIC."commit_infos_files"(
    "commit_info_id" VARCHAR NOT NULL,
    "filename" VARCHAR NOT NULL,
    "status" VARCHAR NOT NULL,
    "patch" VARCHAR NOT NULL
);
-- 7122 +/- SELECT COUNT(*) FROM PUBLIC."commit_infos_files";
CREATE CACHED TABLE PUBLIC."commit_infos_parents"(
    "commit_info_id" VARCHAR NOT NULL,
    "parent" VARCHAR NOT NULL
);
-- 1673 +/- SELECT COUNT(*) FROM PUBLIC."commit_infos_parents";
CREATE CACHED TABLE PUBLIC."commit_review_tasks"(
    "commit_id" VARCHAR NOT NULL,
    "user_id" VARCHAR NOT NULL,
    "created_date" TIMESTAMP NOT NULL
);
ALTER TABLE PUBLIC."commit_review_tasks" ADD CONSTRAINT PUBLIC."commit_review_tasks_id" PRIMARY KEY("commit_id", "user_id");
-- 0 +/- SELECT COUNT(*) FROM PUBLIC."commit_review_tasks";
CREATE CACHED TABLE PUBLIC."events"(
    "id" VARCHAR NOT NULL,
    "event_date" TIMESTAMP NOT NULL,
    "event_type" VARCHAR NOT NULL,
    "originating_user_id" VARCHAR NOT NULL
);
ALTER TABLE PUBLIC."events" ADD CONSTRAINT PUBLIC.CONSTRAINT_B PRIMARY KEY("id");
-- 0 +/- SELECT COUNT(*) FROM PUBLIC."events";
CREATE CACHED TABLE PUBLIC."followups"(
    "id" VARCHAR NOT NULL,
    "receiving_user_id" VARCHAR NOT NULL,
    "thread_commit_id" VARCHAR NOT NULL,
    "thread_file_name" VARCHAR,
    "thread_line_number" INTEGER,
    "last_reaction_id" VARCHAR NOT NULL,
    "last_reaction_created_date" TIMESTAMP NOT NULL,
    "last_reaction_author" VARCHAR NOT NULL
);
ALTER TABLE PUBLIC."followups" ADD CONSTRAINT PUBLIC.CONSTRAINT_2 PRIMARY KEY("id");
-- 0 +/- SELECT COUNT(*) FROM PUBLIC."followups";
CREATE CACHED TABLE PUBLIC."followups_reactions"(
    "followup_id" VARCHAR NOT NULL,
    "reaction_id" VARCHAR NOT NULL
);
ALTER TABLE PUBLIC."followups_reactions" ADD CONSTRAINT PUBLIC.FOLLOWUP_REACTIONS_PK PRIMARY KEY("followup_id", "reaction_id");
-- 0 +/- SELECT COUNT(*) FROM PUBLIC."followups_reactions";
CREATE CACHED TABLE PUBLIC."invitations"(
    "code" VARCHAR NOT NULL,
    "invitation_sender" VARCHAR NOT NULL,
    "expiry_date" TIMESTAMP NOT NULL
);
ALTER TABLE PUBLIC."invitations" ADD CONSTRAINT PUBLIC.CONSTRAINT_A PRIMARY KEY("code");
-- 0 +/- SELECT COUNT(*) FROM PUBLIC."invitations";
CREATE CACHED TABLE PUBLIC."likes"(
    "id" VARCHAR NOT NULL,
    "commit_id" VARCHAR NOT NULL,
    "author_id" VARCHAR NOT NULL,
    "posting_time" TIMESTAMP NOT NULL,
    "file_name" VARCHAR,
    "line_number" INTEGER
);
ALTER TABLE PUBLIC."likes" ADD CONSTRAINT PUBLIC.CONSTRAINT_6 PRIMARY KEY("id");
-- 0 +/- SELECT COUNT(*) FROM PUBLIC."likes";
CREATE CACHED TABLE PUBLIC."users_last_notifs"(
    "user_id" VARCHAR NOT NULL,
    "last_commits_dispatch" TIMESTAMP,
    "last_followups_dispatch" TIMESTAMP
);
ALTER TABLE PUBLIC."users_last_notifs" ADD CONSTRAINT PUBLIC.CONSTRAINT_AA PRIMARY KEY("user_id");
-- 1 +/- SELECT COUNT(*) FROM PUBLIC."users_last_notifs";
CREATE CACHED TABLE PUBLIC."users_settings"(
    "user_id" VARCHAR NOT NULL,
    "avatar_url" VARCHAR NOT NULL,
    "email_notif" BOOLEAN NOT NULL,
    "email_daily_updates" BOOLEAN NOT NULL,
    "app_tour_done" BOOLEAN NOT NULL
);
ALTER TABLE PUBLIC."users_settings" ADD CONSTRAINT PUBLIC.CONSTRAINT_C PRIMARY KEY("user_id");
-- 1 +/- SELECT COUNT(*) FROM PUBLIC."users_settings";
CREATE CACHED TABLE PUBLIC."users_authentications"(
    "user_id" VARCHAR NOT NULL,
    "provider" VARCHAR NOT NULL,
    "username" VARCHAR NOT NULL,
    "username_lowercase" VARCHAR NOT NULL,
    "token" VARCHAR NOT NULL,
    "salt" VARCHAR NOT NULL
);
ALTER TABLE PUBLIC."users_authentications" ADD CONSTRAINT PUBLIC.CONSTRAINT_8 PRIMARY KEY("user_id");
-- 1 +/- SELECT COUNT(*) FROM PUBLIC."users_authentications";
CREATE CACHED TABLE PUBLIC."users"(
    "id" VARCHAR NOT NULL,
    "name" VARCHAR NOT NULL,
    "email_lowercase" VARCHAR NOT NULL,
    "token" VARCHAR NOT NULL,
    "regular" BOOLEAN NOT NULL
);
ALTER TABLE PUBLIC."users" ADD CONSTRAINT PUBLIC.CONSTRAINT_6A PRIMARY KEY("id");
-- 1 +/- SELECT COUNT(*) FROM PUBLIC."users";
CREATE CACHED TABLE PUBLIC."repository_statuses"(
    "repository_name" VARCHAR NOT NULL,
    "head_id" VARCHAR,
    "ready" BOOLEAN NOT NULL,
    "error" VARCHAR
);
ALTER TABLE PUBLIC."repository_statuses" ADD CONSTRAINT PUBLIC.CONSTRAINT_BF PRIMARY KEY("repository_name");
-- 1 +/- SELECT COUNT(*) FROM PUBLIC."repository_statuses";
CREATE CACHED TABLE PUBLIC."heartbeats"(
    "user_id" VARCHAR NOT NULL,
    "last_heartbeat" TIMESTAMP NOT NULL
);
ALTER TABLE PUBLIC."heartbeats" ADD CONSTRAINT PUBLIC.CONSTRAINT_7 PRIMARY KEY("user_id");
-- 0 +/- SELECT COUNT(*) FROM PUBLIC."heartbeats";
ALTER TABLE PUBLIC."followups_reactions" ADD CONSTRAINT PUBLIC.FOLLOWUP_REACTIONS_FK FOREIGN KEY("followup_id") REFERENCES PUBLIC."followups"("id") ON DELETE CASCADE ON UPDATE CASCADE NOCHECK;
ALTER TABLE PUBLIC."users" ADD CONSTRAINT PUBLIC.LAST_NOTIFS_FK FOREIGN KEY("id") REFERENCES PUBLIC."users_last_notifs"("user_id") ON DELETE CASCADE ON UPDATE CASCADE NOCHECK;
ALTER TABLE PUBLIC."commit_infos_parents" ADD CONSTRAINT PUBLIC.COMMIT_INFO_PARENTS_FK FOREIGN KEY("commit_info_id") REFERENCES PUBLIC."commit_infos"("id") NOCHECK;
ALTER TABLE PUBLIC."commit_infos_files" ADD CONSTRAINT PUBLIC.COMMIT_INFO_FILES_FK FOREIGN KEY("commit_info_id") REFERENCES PUBLIC."commit_infos"("id") NOCHECK;
ALTER TABLE PUBLIC."users" ADD CONSTRAINT PUBLIC.SETTINGS_FK FOREIGN KEY("id") REFERENCES PUBLIC."users_settings"("user_id") ON DELETE CASCADE ON UPDATE CASCADE NOCHECK;
ALTER TABLE PUBLIC."users" ADD CONSTRAINT PUBLIC.AUTH_FK FOREIGN KEY("id") REFERENCES PUBLIC."users_authentications"("user_id") ON DELETE CASCADE ON UPDATE CASCADE NOCHECK;
