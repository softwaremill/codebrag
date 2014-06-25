ALTER TABLE "reviewed_commits" DROP CONSTRAINT IF EXISTS "reviewed_commits_id";
ALTER TABLE "reviewed_commits" ADD COLUMN IF NOT EXISTS "repo_name" VARCHAR NOT NULL;
ALTER TABLE "reviewed_commits" ADD CONSTRAINT IF NOT EXISTS "reviewed_commits_id" PRIMARY KEY ("user_id", "sha", "repo_name");