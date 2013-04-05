'use strict';

angular.module('codebrag.followups')

    .controller('FollowupListItemCtrl', function ($scope, currentCommit) {

        $scope.openCommitDetails = function (commit) {
            currentCommit.id = commit.commitId
        }

    });