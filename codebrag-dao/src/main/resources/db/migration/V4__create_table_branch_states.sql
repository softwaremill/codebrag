CREATE TABLE "branch_states"(
    "branch_name" VARCHAR NOT NULL,
    "sha" VARCHAR NOT NULL,
);
ALTER TABLE "branch_states" ADD CONSTRAINT "branch_states_id" PRIMARY KEY("branch_name");
