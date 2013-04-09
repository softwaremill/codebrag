'use strict';

angular.module('codebrag.followups')

    .controller('FollowupListItemCtrl', function ($scope, currentCommit, Followups) {

        $scope.openCommitDetails = function (commit) {
            currentCommit.id = commit.commitId
        }

        $scope.dismiss = function (followup) {
            Followups.remove({id: followup.commit.commitId}, function (response) {
                    console.log("Removed")
                }
            )
        }
    });