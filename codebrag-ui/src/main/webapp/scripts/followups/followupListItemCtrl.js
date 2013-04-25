'use strict';

angular.module('codebrag.followups')

    .controller('FollowupListItemCtrl', function ($scope, $state, $stateParams, followupsListService) {

        $scope.openCommitDetails = function (commit) {
            $state.transitionTo('followups.details', {id: commit.commitId})
        };

        $scope.dismiss = function (followup) {
            var id = followup.commit.commitId;
            followupsListService.removeFollowup(id).then(function() {
                _getOutOfCommitDetailsIfCurrentRemoved(id)
            })
        };

        function _getOutOfCommitDetailsIfCurrentRemoved(commitId) {
            if (commitId === $stateParams.id) {
                $state.transitionTo('followups.list');
            }
        }


    });