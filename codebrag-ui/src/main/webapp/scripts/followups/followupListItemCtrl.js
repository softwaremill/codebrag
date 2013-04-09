'use strict';

angular.module('codebrag.followups')

    .controller('FollowupListItemCtrl', function ($scope, $state, Followups) {

        $scope.openCommitDetails = function (commit) {
            $state.transitionTo('followups.details', {id: commit.commitId})
        }

        $scope.dismiss = function (followup) {
            Followups.remove({id: followup.commit.commitId}, function (response) {
                    console.log("Removed")
                }
            )
        }
    });