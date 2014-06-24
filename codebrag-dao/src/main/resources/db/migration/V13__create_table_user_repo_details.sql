CREATE TABLE IF NOT EXISTS "user_repo_details"(
    "user_id" VARCHAR NOT NULL,
    "repo_name" VARCHAR NOT NULL,
    "branch_name" VARCHAR NOT NULL,
    "to_review_since" TIMESTAMP NOT NULL,
    "default" BOOLEAN NOT NULL DEFAULT FALSE,
);
ALTER TABLE "user_repo_details" ADD CONSTRAINT IF NOT EXISTS "user_repo_branch_pk" PRIMARY KEY("user_id", "repo_name", "branch_name");
