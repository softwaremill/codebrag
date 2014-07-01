ALTER TABLE "branch_states" DROP CONSTRAINT IF EXISTS "branch_states_id";
ALTER TABLE "branch_states" ADD COLUMN IF NOT EXISTS "repo_name" VARCHAR NOT NULL DEFAULT '';
ALTER TABLE "branch_states" ADD CONSTRAINT IF NOT EXISTS "repo_branch_state" UNIQUE ("repo_name", "branch_name");

ALTER TABLE "commit_infos" DROP CONSTRAINT IF EXISTS "unique_sha";
ALTER TABLE "commit_infos" ADD COLUMN IF NOT EXISTS "repo_name" VARCHAR NOT NULL DEFAULT '';
ALTER TABLE "commit_infos" ADD CONSTRAINT IF NOT EXISTS "repo_sha" UNIQUE ("repo_name", "sha");
