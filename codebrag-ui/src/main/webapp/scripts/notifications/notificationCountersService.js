angular.module('codebrag.notifications')

    .service('notificationCountersService', function ($http, $rootScope, events) {

        $rootScope.$on(events.commitCountChanged, function(event, data) {
            _updateCommits(data.commitCount)
        });

        $rootScope.$on(events.followupCountChanged, function(event, data) {
            _updateFollowups(data.followupCount)
        });

        $rootScope.$on(events.loggedIn, function() {
            _loadCountersFromServer()
        });

        var counterValues = {
            commits: 0,
            followups: 0
        };

        function counters() {
            return counterValues;
        }

        function _updateFollowups(newCount) {
            counterValues.followups = newCount;
        }

        function _updateCommits(newCount) {
            counterValues.commits = newCount;
        }

        function _loadCountersFromServer() {
            $http.get('rest/notificationCounts').then(function(response) {
                counterValues.commits = response.data.pendingCommitCount;
                counterValues.followups = response.data.followupCount;
            });
        }

        return {
            counters: counters
        }
    });