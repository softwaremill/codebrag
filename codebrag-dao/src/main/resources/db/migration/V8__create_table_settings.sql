CREATE TABLE "settings"(
    "key" VARCHAR NOT NULL,
    "value" VARCHAR NOT NULL,
);
ALTER TABLE "settings" ADD CONSTRAINT "settings_id" PRIMARY KEY("key");
