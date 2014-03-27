CREATE TABLE "reviewed_commits"(
    "user_id" VARCHAR NOT NULL,
    "sha" VARCHAR NOT NULL,
    "review_date" TIMESTAMP NOT NULL,
);
ALTER TABLE "reviewed_commits" ADD CONSTRAINT "reviewed_commits_id" PRIMARY KEY("user_id", "sha");
