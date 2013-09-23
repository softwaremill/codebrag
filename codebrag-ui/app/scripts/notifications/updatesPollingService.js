angular.module('codebrag.notifications')

    .service('updatesPollingService', function ($timeout, $http, $rootScope) {

        var defaultInterval = 30000;
        var lastUpdateTimestamp;

        var self = this;

        this.updatesReceivedEvent = 'updatesReceived';

        this.startPolling = function() {
            $timeout(pollForStats, defaultInterval);
        };

        function pollForStats() {
            var config = {};
            if(lastUpdateTimestamp) {
                config.params = {};
                config.params.since = lastUpdateTimestamp;
            }
            $http.get('/rest/updates', config).then(success, error);
        }

        function success(response) {
            lastUpdateTimestamp = response.data.lastUpdateTimestamp;
            var updates = {commits: response.data.commits, followups: response.data.followups};
            $rootScope.$broadcast(self.updatesReceivedEvent, updates);
            scheduleNextCall();
        }

        function error() {
            scheduleNextCall();
        }

        function scheduleNextCall() {
            self.startPolling();
        }

    });