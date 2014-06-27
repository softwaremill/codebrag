angular.module('codebrag.counters').factory('countersService', function($http, $timeout, $rootScope, Counter, events, currentRepoContext) {

    var pollingInterval = 60000,
        syncTimer,
        commitsCounter = new Counter(),
        followupsCounter = new Counter();

    currentRepoContext.ready().then(function() {
        initPolling({ commits: true, followups: true });
        bindEventListeners();
    });

    function fetchCounters(repo, branch) {
        return $http.get('rest/updates', {params: {repo: repo, branch: branch}}).then(function(resp) {
            return {
                commits: resp.data.commits,
                followups: resp.data.followups
            }
        });
    }

    function initPolling(which) {
        var repo = currentRepoContext.repo,
            branch = currentRepoContext.branch;
        fetchCounters(repo, branch)
            .then(reInitializeCounters(which), angular.noop)
            .then(scheduleNextSync);

        function scheduleNextSync() {
            syncTimer && $timeout.cancel(syncTimer);
            syncTimer = $timeout(function() {
                fetchCounters(repo, branch)
                    .then(updateIncomingCounters)
                    .then(scheduleNextSync)
            }, pollingInterval);
        }
    }

    function reInitializeCounters(which) {
        return function(initialCounters) {
            if(angular.isUndefined(which) || which.commits) {
                commitsCounter.reInitialize(initialCounters.commits);
            }
            if(angular.isUndefined(which) || which.followups) {
                followupsCounter.reInitialize(initialCounters.followups);
            }
        }
    }

    function updateIncomingCounters(incomingCounters) {
        commitsCounter.setIncomingTo(incomingCounters.commits);
        followupsCounter.setIncomingTo(incomingCounters.followups);
    }

    function bindEventListeners() {
        $rootScope.$on(events.branches.branchChanged, function() {
            initPolling({commits: true});
        });
        $rootScope.$on(events.commitsListFilterChanged, function() {
            initPolling({commits: true});
        });
        $rootScope.$on(events.commitReviewed, function() {
            commitsCounter.decrease();
        });
        $rootScope.$on(events.followupDone, function() {
            followupsCounter.decrease();
        });
        $rootScope.$on(events.nextCommitsLoaded, function() {
            commitsCounter.replace();
        });
        $rootScope.$on(events.previousCommitsLoaded, function() {
            commitsCounter.replace();
        });
    }

    return {
        commitsCounter: commitsCounter,
        followupsCounter: followupsCounter,
        reloadCounters: initPolling
    }

});