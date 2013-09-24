angular.module('codebrag.notifications')

    .factory('notificationCountersService', function ($http, $rootScope, events) {

        var counters = {
            commitsCount: 0,
            followupsCount: 0
        };

        $rootScope.$on(events.commitCountChanged, function(event, data) {
            counters.commitsCount = data.commitCount;
        });

        $rootScope.$on(events.followupCountChanged, function(event, data) {
            counters.followupsCount = data.followupCount;
        });

        $rootScope.$on(events.loggedIn, function() {
            _loadCountersFromServer();
        });

        function _loadCountersFromServer() {
            $http.get('rest/notificationCounts').then(function(response) {
                counters.commitsCount = response.data.pendingCommitCount;
                counters.followupsCount = response.data.followupCount;
            });
        }

        return {
            counters: counters
        };

    });