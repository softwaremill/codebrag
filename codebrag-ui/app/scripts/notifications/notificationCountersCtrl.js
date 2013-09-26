angular.module('codebrag.notifications')

    .controller('NotificationCountersCtrl', function ($scope, notificationService) {

        $scope.counters = notificationService.counters;
    });