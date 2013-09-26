angular.module('codebrag.notifications')

    .factory('notificationService', function ($http, $rootScope, events, $timeout) {

        var timer;

        var defaultPollingInterval = 30000; // milliseconds

        var counters = {
            commitsCount: 0,
            followupsCount: 0
        };

        $rootScope.$on(events.loginRequired, function() {
            timer && $timeout.cancel(timer);
        });

        $rootScope.$on(events.loggedIn, function() {
            askForUpdates().then(function(response) {
                counters.commitsCount = response.data.commits;
                counters.followupsCount = response.data.followups;
                timer = $timeout(pollForStats, defaultPollingInterval);
            });
        });

        $rootScope.$on(events.commitReviewed, function() {
            counters.commitsCount -= 1;
        });

        $rootScope.$on(events.followupDone, function() {
            counters.followupsCount -= 1;
        });

        $rootScope.$on(events.refreshCommitsCounter, function() {
            askForUpdates().then(function(response) {
                counters.commitsCount = response.data.commits;
                $rootScope.$broadcast(events.updatesWaiting, calculateDelta(response.data));
            });
        });

        $rootScope.$on(events.refreshFollowupsCounter, function() {
            askForUpdates().then(function(response) {
                counters.followupsCount = response.data.followups;
                $rootScope.$broadcast(events.updatesWaiting, calculateDelta(response.data));
            });
        });

        function askForUpdates() {
            return $http.get('rest/updates')
        }

        function calculateDelta(incomingData) {
            return {
                commits: (+incomingData.commits) - counters.commitsCount,
                followups: (+incomingData.followups) - counters.followupsCount
            };
        }

        function pollForStats() {
            askForUpdates().then(success, error);

            function success(response) {
                $rootScope.$broadcast(events.updatesWaiting, calculateDelta(response.data));
                scheduleNextCall();
            }

            function error() {
                scheduleNextCall();
            }

            function scheduleNextCall() {
                timer = $timeout(pollForStats, defaultPollingInterval);
            }
        }

        return {
            counters: counters
        };

    });