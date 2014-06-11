ALTER TABLE "branch_states" ADD COLUMN IF NOT EXISTS "repo_name" VARCHAR NOT NULL;
ALTER TABLE "branch_states" DROP CONSTRAINT IF EXISTS "branch_states_id";
ALTER TABLE "branch_states" ADD CONSTRAINT IF NOT EXISTS "repo_branch_state" UNIQUE ("repo_name", "branch_name");
