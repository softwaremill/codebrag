CREATE TABLE IF NOT EXISTS "user_observed_branches"(
    "id" VARCHAR NOT NULL,
    "user_id" VARCHAR NOT NULL,
    "repo_name" VARCHAR NOT NULL,
    "branch_name" VARCHAR NOT NULL
);
ALTER TABLE "user_observed_branches" ADD CONSTRAINT IF NOT EXISTS "user_alias_pk" PRIMARY KEY("id");
ALTER TABLE "user_observed_branches" ADD CONSTRAINT IF NOT EXISTS "unique_user_repo_branch" UNIQUE("user_id", "repo_name", "branch_name");
