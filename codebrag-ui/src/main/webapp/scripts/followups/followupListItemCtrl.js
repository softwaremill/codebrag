'use strict';

angular.module('codebrag.followups')

    .controller('FollowupListItemCtrl', function ($scope, $state, $stateParams, followupsListService) {

        $scope.openFollowupDetails = function (followup) {
            $state.transitionTo('followups.details', {followupId: followup.followupId, commentId: followup.comment.commentId})
        };

        $scope.dismiss = function (followup) {
            var id = followup.followupId;
            followupsListService.removeFollowup(id).then(function() {
                _getOutOfFollowupDetailsIfCurrentRemoved(id)
            })
        };

        function _getOutOfFollowupDetailsIfCurrentRemoved(followupId) {
            if (followupId === $stateParams.followupId) {
                $state.transitionTo('followups.list');
            }
        }


    });