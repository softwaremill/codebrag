CREATE TABLE IF NOT EXISTS "user_aliases"(
    "id" VARCHAR NOT NULL,
    "user_id" VARCHAR NOT NULL,
    "alias" VARCHAR NOT NULL
);
ALTER TABLE "user_aliases" ADD CONSTRAINT IF NOT EXISTS "user_alias_pk" PRIMARY KEY("id");
ALTER TABLE "user_aliases" ADD CONSTRAINT IF NOT EXISTS "unique_alias" UNIQUE("alias");
