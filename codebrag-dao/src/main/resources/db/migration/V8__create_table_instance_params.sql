CREATE TABLE "instance_params"(
    "key" VARCHAR NOT NULL,
    "value" VARCHAR NOT NULL,
);
ALTER TABLE "instance_params" ADD CONSTRAINT "instance_params_id" PRIMARY KEY("key");
