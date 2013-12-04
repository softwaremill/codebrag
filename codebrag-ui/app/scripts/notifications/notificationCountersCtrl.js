angular.module('codebrag.notifications')

    .controller('NotificationCountersCtrl', function ($scope, notificationService, $state, $rootScope, events) {

        $scope.counters = notificationService.counters;

        $scope.openFollowups = function() {
            $rootScope.$broadcast(events.reloadFollowupsList);
            $rootScope.$broadcast(events.expandList);
            $state.transitionTo('followups.list');
        };

        $scope.openCommits = function() {
            $rootScope.$broadcast(events.reloadCommitsList);
            $rootScope.$broadcast(events.expandList);
            $state.transitionTo('commits.list');
        };
    });