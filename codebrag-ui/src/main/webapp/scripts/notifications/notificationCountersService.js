angular.module('codebrag.notifications')

    .service('notificationCountersService', function ($http, $rootScope) {

        $rootScope.$on('codebrag:commitCountChanged', function(event, data) {
            _updateCommits(data.commitCount)
        });

        $rootScope.$on('codebrag:followupCountChanged', function(event, data) {
            _updateFollowups(data.followupCount)
        });

        var counterValues = {
            loaded: false,
            commits: 0,
            followups: 0
        };

        function counters() {
            if (!counterValues.loaded) {
                _loadCountersFromServer()
            }
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
                counterValues.loaded = true;
            });
        }

        return {
            counters: counters
        }
    });