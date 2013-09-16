#!/bin/bash

# Variables

CURL="curl -s -S"
BASE_URL="http://localhost:8080"
USERS_URL="$BASE_URL/rest/users"
COMMITS_URL="$BASE_URL/rest/commits"
SYNC_URL="$BASE_URL/sync"
CONTENT_TYPE_HEADER="Content-type: application/json"
ACCEPT_HEADER="Accept: application/json"

# Functions

function sha_to_commitId {
    local sha=$1
    local commitId=$(mongo codebrag --eval "db.commit_infos.find({\"sha\":\"$sha\"},{\"sha\":1}).forEach(function(x){print(x._id);})" \
            | tail -c 27 | head -c 24)
    echo $commitId
}

function comment {
    local sha=$1
    local commitId=$(sha_to_commitId ${sha})
    local data=$2
    ${CURL} -b cookies.txt -H "$CONTENT_TYPE_HEADER" -H "$ACCEPT_HEADER" -X POST \
        -d "$data" \
        "$COMMITS_URL/$commitId/comments" > /dev/null
}

function comment_commit {
    local sha=$1
    local user=$2
    local message=$3
    echo "Placing comment on commit $sha"
    comment $sha "{\"userId\":\"$user\",\"body\":\"$message\"}"
}

function comment_line {
    local sha=$1
    local user=$2
    local fileName=$3
    local lineNumber=$4
    local message=$5
    echo "Placing comment on line $lineNumber of file $fileName in commit $sha"

    comment $sha "{\"userId\":\"$user\",\"body\":\"$message\",\"fileName\":\"$fileName\",\"lineNumber\":$lineNumber}"
}

function like {
    local sha=$1
    local commitId=$(sha_to_commitId $sha)
    local data=$2

    ${CURL} -b cookies.txt -H "$CONTENT_TYPE_HEADER" -H "$ACCEPT_HEADER" -X POST \
          -d "$data" \
          "$COMMITS_URL/$commitId/likes" > /dev/null
}

function like_commit {
    local sha=$1
    local user=$2
    echo "Liking commit $sha"

    like $sha "{\"userId\":\"$user\"}"
}

function like_line {
    local sha=$1
    local user=$2
    local fileName=$3
    local lineNumber=$4
    echo "Liking line $lineNumber of file $fileName in commit $sha"

    like $sha "{\"user\":\"$user\",\"fileName\":\"$fileName\",\"lineNumber\":$lineNumber}"
}

function login {
    local user=$1
    local pass=$2

    echo $(${CURL} -c cookies.txt -H "$CONTENT_TYPE_HEADER" -H "$ACCEPT_HEADER" -X POST -d "{\"login\":\"$user\",\"password\":\"$pass\"}" ${USERS_URL} | head -c 31 | tail -c 24)
}

function logout {
    ${CURL} -b cookies.txt "$USERS_URL/logout"
}

function synchronize {
    ${CURL} ${SYNC_URL}
}

###############################################################################

# clear everything before start
echo "Cleaning up database"
mongo codebrag --eval "db.dropDatabase()"

# add demo users
echo "Importing demo users"
mongoimport -d codebrag -c users demo_users.json

# sync repository
echo "Synchronizing repository"
if [[ $(synchronize) =~ "Repository synchronized" ]]
then
    echo "Logging in as user 'fox'"
    user=$(login "fox" "codebrag")

    comment_commit d7ac6aadb937e6eca61df53a0016fb2c019ebe1c "$user" "Test comment"
    comment_line d7ac6aadb937e6eca61df53a0016fb2c019ebe1c "$user" "src/test/scala/com/softwaremill/gameoflife/BoardTest.scala" 57 "You could use 'should equal(...)' instead of 'should be ===' for more readability"
    like_commit d7ac6aadb937e6eca61df53a0016fb2c019ebe1c "$user"
    like_line d7ac6aadb937e6eca61df53a0016fb2c019ebe1c "$user" "src/test/scala/com/softwaremill/gameoflife/BoardTest.scala" 57

    echo "Logging fox out"
    logout

    # clear left overs
    rm cookies.txt
else
    echo "Synchronization not successful"
fi


