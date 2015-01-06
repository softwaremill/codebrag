angular.module('codebrag.dashboard')

    .controller('DashboardListItemCtrl', function ($scope, $state, $stateParams, allFollowupsService, $rootScope, events) {

        $scope.openFollowupDetails = function (followup) {
            if(_thisFollowupOpened(followup)) {
                $rootScope.$broadcast(events.scrollOnly);
            } else {
                $state.transitionTo('dashboard.details', {followupId: followup.followupId, commentId: followup.lastReaction.reactionId});
            }
        };

        $scope.dismiss = function (followup) {
            allfollowupsService.removeAndGetNext(followup.followupId).then(function(nextFollowup) {
                if(nextFollowup) {
                    $state.transitionTo('dashboard.details', {followupId: nextFollowup.followupId, commentId: nextFollowup.lastReaction.reactionId});
                } else {
                    $state.transitionTo('dashboard.list');
                }
            });
        };
 
        function _thisFollowupOpened(followup) {
            return $state.current.name === 'dashboard.details' && $state.params.followupId === followup.followupId;
        }


    });
