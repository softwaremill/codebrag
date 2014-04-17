angular.module('codebrag.counters').factory('countersService', function($http, $timeout, $rootScope, Counter, branchesService, events) {

    var pollingInterval = 10000,
        commitsCounter = new Counter(),
        followupsCounter = new Counter();


    function fetchCounters(branch) {
        return $http.get('rest/updates', {params: {branch: branch}}).then(function(resp) {
            return {
                commits: resp.data.commits,
                followups: resp.data.followups
            }
        });
    }

    function initPolling(which, branch) {
        var currentBranch = branch || branchesService.selectedBranch();
        initPolling.syncTimer && $timeout.cancel(initPolling.syncTimer);
        fetchCounters(currentBranch)
            .then(reInitializeCounters(which), angular.noop)
            .then(scheduleNextSync);

        function scheduleNextSync() {
            initPolling.syncTimer = $timeout(function() {
                fetchCounters(currentBranch)
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
        $rootScope.$on(events.branches.branchChanged, function(e, branch) {
            initPolling({commits: true}, branch);
        });
        $rootScope.$on('codebrag:commitsListFilterChanged', function(e, branch) {
            console.log('init polling');
            initPolling({commits: true}, branch);
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

    branchesService.ready().then(function() {
        initPolling();
        bindEventListeners();
    });

    return {
        commitsCounter: commitsCounter,
        followupsCounter: followupsCounter,
        reloadCounters: initPolling
    }

});