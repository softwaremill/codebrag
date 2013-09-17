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
    comment_line 0644648add101cfaa78b029b6418f6f6ba8faa40 ${user} "src/main/scala/com/softwaremill/gameoflife/examples/Toad.scala" 17 "There is too much noise in this line for me. Especially those magic numbers at the end. Maybe extract this Rectangle() thing somehow?"
    comment_commit 058feadef36e583108b6da4b3f9913f3c126e037 ${user} "Nice and simple way to present board. I like it."
    comment_line 4efad7b08f519dcd8bb301ce72c166d77b245647 ${user} "src/main/scala/com/softwaremill/gameoflife/BoardReader.scala" 12 "I'd extract those comment characters list to some constant for better visibility."
    comment_line 4efad7b08f519dcd8bb301ce72c166d77b245647 ${user} "src/main/scala/com/softwaremill/gameoflife/BoardReader.scala" 18 "Why not add more descriptive names instead of s and i?"
    like_commit 62dfc47c177f942dde6afc45f378f92a77fc77a6 ${user}
    like_line 058feadef36e583108b6da4b3f9913f3c126e037 ${user} "src/main/scala/com/softwaremill/gameoflife/Board.scala" 8
    logout

    user=$(login "scully" "codebrag")
    comment_line e775bb86da84a9d283849969e7e090d58832c59a ${user} "src/main/scala/com/softwaremill/gameoflife/Board.scala" 27 "This on method name doesn't sound good enough for me. What about putOn or setOn?"
    comment_commit 4efad7b08f519dcd8bb301ce72c166d77b245647 ${user} "Extract commonly used cells (in tests) to some named things?"
    comment_commit cbdac3863700935500e390899a1733114a866d50 ${user} "Those s, j, i names... Why not name it better?"
    comment_commit e7c6c4dc6721397b5102535d8da901fd72085f73 ${user} "Love this ExampleRunner idea!"
    like_commit e7c6c4dc6721397b5102535d8da901fd72085f73 ${user}
    logout

    user=$(login "skinner" "codebrag")
    comment_line e775bb86da84a9d283849969e7e090d58832c59a ${user} "src/main/scala/com/softwaremill/gameoflife/Board.scala" 27 "I agree. putOn sounds much better :)"
    comment_commit e775bb86da84a9d283849969e7e090d58832c59a ${user} "There are many places where you name your values like b or c. It would be much more readable if you chose some more descriptive names, like just board or cell."
    comment_line e7c6c4dc6721397b5102535d8da901fd72085f73 ${user} "src/main/scala/com/softwaremill/gameoflife/examples/ExampleRunner.scala" 15 "I really like this zipWithIndex technique :)"
    like_commit cbdac3863700935500e390899a1733114a866d50 ${user}
    like_line e7c6c4dc6721397b5102535d8da901fd72085f73 ${user} "src/main/scala/com/softwaremill/gameoflife/examples/ExampleRunner.scala" 15
    logout

    user=$(login "fox" "codebrag")
    comment_commit e775bb86da84a9d283849969e7e090d58832c59a ${user} "You are right, I'll change it."
    comment_commit d7ac6aadb937e6eca61df53a0016fb2c019ebe1c ${user} "There is a lot of repetition of Cell(0, 0). Why not extracting it to well-named constant for all tests?"
    logout

    user=$(login "skinner" "codebrag")
    comment_line d7ac6aadb937e6eca61df53a0016fb2c019ebe1c ${user} "src/test/scala/com/softwaremill/gameoflife/BoardPrinterTest.scala" 20 "I guess that === is not needed here. Simply should be(...) works fine."
    logout

    user=$(login "fox" "codebrag")
    comment_line d7ac6aadb937e6eca61df53a0016fb2c019ebe1c ${user} "src/test/scala/com/softwaremill/gameoflife/BoardPrinterTest.scala" 20 "Or just should equal(...). I guess they are two alternative ways."
    logout

    # clear left overs
    rm cookies.txt
else
    echo "Synchronization not successful"
fi


