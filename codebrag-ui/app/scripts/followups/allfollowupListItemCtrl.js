angular.module('codebrag.allfollowups')

    .controller('AllFollowupListItemCtrl', function ($scope, $state, $stateParams, allfollowupsService, $rootScope, events) {

        $scope.openFollowupDetails = function (followup) {
            if(_thisFollowupOpened(followup)) {
                $rootScope.$broadcast(events.scrollOnly);
            } else {
                $state.transitionTo('allfollowups.details', {followupId: followup.followupId, commentId: followup.lastReaction.reactionId});
            }
        };

        $scope.dismiss = function (followup) {
            allfollowupsService.removeAndGetNext(followup.followupId).then(function(nextFollowup) {
                if(nextFollowup) {
                    $state.transitionTo('allfollowups.details', {followupId: nextFollowup.followupId, commentId: nextFollowup.lastReaction.reactionId});
                } else {
                    $state.transitionTo('allfollowups.list');
                }
            });
        };
 
        function _thisFollowupOpened(followup) {
            return $state.current.name === 'allfollowups.details' && $state.params.followupId === followup.followupId;
        }


    });
