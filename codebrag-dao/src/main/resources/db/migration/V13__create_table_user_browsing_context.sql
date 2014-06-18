CREATE TABLE "user_browsing_contexts"(
    "user_id" VARCHAR NOT NULL,
    "repo_name" VARCHAR NOT NULL,
    "branch_name" VARCHAR NOT NULL,
    "default" BOOLEAN NOT NULL DEFAULT FALSE,
);
ALTER TABLE "user_browsing_contexts" ADD CONSTRAINT "user_repo_branch_pk" PRIMARY KEY("user_id", "repo_name", "branch_name");
