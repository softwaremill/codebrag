angular.module('codebrag.notifications')

    .controller('NotificationCountersCtrl', function ($scope, notificationCountersService) {

        $scope.counters = notificationCountersService.counters();
    });