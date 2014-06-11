ALTER TABLE "branch_states" ADD COLUMN IF NOT EXISTS "repo_name" VARCHAR NOT NULL;
ALTER TABLE "branch_states" DROP CONSTRAINT "branch_states_id";
ALTER TABLE "branch_states" ADD CONSTRAINT "repo_branch_state" UNIQUE ("repo_name", "branch_name");
