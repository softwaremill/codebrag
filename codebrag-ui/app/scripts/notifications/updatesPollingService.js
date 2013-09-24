angular.module('codebrag.notifications')

    .service('updatesPollingService', function ($timeout, $http, $rootScope, events) {

        var defaultInterval = 30000;
        var lastUpdateTimestamp;

        var self = this;

        this.startPolling = function() {
            $timeout(pollForStats, defaultInterval);
        };

        function pollForStats() {
            var config = {};
            if(lastUpdateTimestamp) {
                config.params = {since: lastUpdateTimestamp};
            }
            $http.get('/rest/updates', config).then(success, error);
        }

        function success(response) {
            lastUpdateTimestamp = response.data.lastUpdateTimestamp;
            var updates = {
                commits: response.data.commits,
                followups: response.data.followups
            };
            $rootScope.$broadcast(events.updatesWaiting, updates);
            scheduleNextCall();
        }

        function error() {
            scheduleNextCall();
        }

        function scheduleNextCall() {
            self.startPolling();
        }

    });