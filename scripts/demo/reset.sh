#!/bin/bash

# Variables

FOX_ID="51c0bbdf30044fa106947635"
BASE_URL="http://localhost:8080"
USERS_URL="$BASE_URL/rest/users"
COMMITS_URL="$BASE_URL/rest/commits"
CONTENT_TYPE_HEADER="Content-type: application/json"
ACCEPT_HEADER="Accept: application/json"

# Functions

function sha_to_commitId {
    local sha=$1
    echo $(mongo codebrag --eval "db.commit_infos.find({\"sha\":\"$sha\"},{\"sha\":1}).forEach(function(x){print(x._id);})" \
            | tail -c 27 | head -c 24)
}

function comment {
    local sha=$1
    local commitId=$(sha_to_commitId $sha)
    local data=$2
    curl -v -b cookies.txt -H "$CONTENT_TYPE_HEADER" -H "$ACCEPT_HEADER" -X POST \
        -d "$data" \
        "$COMMITS_URL/$commitId/comments"
}

function comment_commit {
    local sha=$1
    local user=$2
    local message=$3

    comment $sha "{\"userId\":\"$user\",\"body\":\"$message\"}"
}

function comment_line {
    local sha=$1
    local user=$2
    local fileName=$3
    local lineNumber=$4
    local message=$5

    comment $sha "{\"userId\":\"$user\",\"body\":\"$message\",\"fileName\":\"$fileName\",\"lineNumber\":$lineNumber}"
}

function like {
    local sha=$1
    local commitId=$(sha_to_commitId $sha)
    local data=$2
    curl -v -b cookies.txt -H "$CONTENT_TYPE_HEADER" -H "$ACCEPT_HEADER" -X POST \
          -d "$data" \
          "$COMMITS_URL/$commitId/likes"
}

function like_commit {
    local sha=$1
    local user=$2

    like $sha "{\"userId\":\"$user\"}"
}

function like_line {
    local sha=$1
    local user=$2
    local fileName=$3
    local lineNumber=$4

    like $sha "{\"user\":\"$user\",\"fileName\":\"$fileName\",\"lineNumber\":$lineNumber}"
}

###############################################################################

# clear everything before start
rm cookies.txt
mongo codebrag --eval "db.dropDatabase()"

# add demo users
mongoimport -d codebrag -c users demo_users.json

# wait for next synchronization
sleep 45

curl -v -c cookies.txt -H "$CONTENT_TYPE_HEADER" -H "$ACCEPT_HEADER" -X POST -d '{"login":"fox","password":"codebrag"}' $USERS_URL

comment_commit d7ac6aadb937e6eca61df53a0016fb2c019ebe1c "$FOX_ID" "Test comment"
comment_line "d7ac6aadb937e6eca61df53a0016fb2c019ebe1c" "$FOX_ID" "src/test/scala/com/softwaremill/gameoflife/BoardTest.scala" 57 "You could use 'should equal(...)' instead of 'should be ===' for more readability"
like_commit d7ac6aadb937e6eca61df53a0016fb2c019ebe1c "$FOX_ID"
like_line d7ac6aadb937e6eca61df53a0016fb2c019ebe1c "$FOX_ID" "src/test/scala/com/softwaremill/gameoflife/BoardTest.scala" 57



