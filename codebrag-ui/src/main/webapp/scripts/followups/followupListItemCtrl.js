'use strict';

angular.module('codebrag.followups')

    .controller('FollowupListItemCtrl', function ($scope, $state, $stateParams, followupsService, $rootScope, events) {

        $scope.openFollowupDetails = function (followup) {
            if(_thisFollowupOpened(followup)) {
                $rootScope.$broadcast(events.scrollOnly);
            } else {
                $state.transitionTo('followups.details', {followupId: followup.followupId, commentId: followup.lastReaction.reactionId})
            }
        };

        $scope.dismiss = function (followup) {
            followupsService.removeAndGetNext(followup.followupId).then(function(nextFollowup) {
                if(nextFollowup) {
                    $state.transitionTo('followups.details', {followupId: nextFollowup.followupId, commentId: nextFollowup.lastReaction.reactionId});
                } else {
                    $state.transitionTo('followups.list');
                }
            });
        };

        function _thisFollowupOpened(followup) {
            return $state.current.name === 'followups.details' && $state.params.followupId === followup.followupId;
        }


    });