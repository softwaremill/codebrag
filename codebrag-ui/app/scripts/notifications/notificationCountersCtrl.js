angular.module('codebrag.notifications')

    .controller('NotificationCountersCtrl', function ($scope, notificationService, $state, $rootScope, events) {

        $scope.counters = notificationService.counters;

        $scope.openFollowups = function() {
            $state.transitionTo('followups.list');
        };

        $scope.openCommits = function() {
            $rootScope.$broadcast(events.reloadCommitsList);
            $state.transitionTo('commits.list');
        };
    });