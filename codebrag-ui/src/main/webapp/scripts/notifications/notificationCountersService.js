angular.module('codebrag.notifications')

    .service('notificationCountersService', function ($http) {

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

        function updateFollowups(newCount) {
            counterValues.followups = newCount;
        }

        function updateCommits(newCount) {
            counterValues.commits = newCount;
        }

        function decreaseCommits() {
            counterValues.commits--;
        }

        function decreateFollowups() {
            counterValues.followups--;
        }

        function _loadCountersFromServer() {
            $http.get('rest/notificationCounts').then(function(response) {
                counterValues.commits = response.data.pendingCommitCount;
                counterValues.followups = response.data.followupCount;
                counterValues.loaded = true;
            });
        }

        return {
            counters: counters,
            updateFollowups: updateFollowups,
            decreaseCommits: decreaseCommits,
            decreaseFollowups: decreateFollowups,
            updateCommits: updateCommits
        }
    });