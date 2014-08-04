angular.module('codebrag.counters')

    .factory('Notification', function() {

        var Notification = function(repo, branch, commitsCount, read) {
            this.repo = repo;
            this.branch = branch;
            this.commitsCount = commitsCount || 0;
            this.read = read || false;
        };

        Notification.prototype = {
            updateCount: function(newCount) {
                this.commitsCount = newCount;
            },
            active: function() {
                return this.commitsCount != 0;
            }
        };

        return Notification;

    })


    .factory('notificationPoller', function($http, $timeout, Notification, notificationsRegistry) {

        var timer,
            delay = 5000;

        function call() {
            fetchNotifications().then(function(notifs) {
                notificationsRegistry.mergeAll(notifs);
                timer = $timeout(call, delay);
            });
        }

        function start() {
            if(!timer) {
                console.log('Notification poller started');
                timer = $timeout(call, 0);
            }
        }

        function stop() {
            $timeout.cancel(timer);
            timer = null;
            console.log('Notification poller stopped');
        }

        function restart() {
            stop();
            start();
        }

        function fetchNotifications() {
            return $http.get('rest/notifications').then(toNotifications);
        }

        function toNotifications(resp) {
            return resp.data.repos.map(function(c) {
                return new Notification(c.repoName, c.branchName, c.commits);
            });
        }

        return {
            start: start,
            stop: stop,
            restart: restart
        };

    })

    .service('notificationsRegistry', function() {

        var notifications = [];

        this.notifications = notifications;

        this.mergeAll = function(notifs) {
            var merge = this.merge.bind(this);
            notifs.forEach(merge);
        };

        this.merge = function(notif) {
            var found = this.find(notif.repo, notif.branch);
            if(found) {
                found.updateCount(notif.commitsCount);
            } else {
                notifications.push(notif);
            }
        };

        this.find = function(repo, branch) {
            return _.find(notifications, identity(repo, branch));
        };

        this.markAsRead = function() {
            notifications.forEach(function(bn) {
                bn.read = true;
            });
        };

        function identity(repo, branch) {
            return function(e) {
                return (e.repo === repo && e.branch === branch);
            }
        }

    });

angular.module('codebrag.counters').factory('countersService', function($http, $timeout, $rootScope, Counter, events, currentRepoContext) {

    var pollingInterval = 60000,
        syncTimer,
        commitsCounter = new Counter(),
        followupsCounter = new Counter();

//    currentRepoContext.ready().then(function() {
//        initPolling({ commits: true, followups: true });
//        bindEventListeners();
//    });

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
//        $rootScope.$on(events.branches.branchChanged, function() {
//            initPolling({commits: true});
//        });
//        $rootScope.$on(events.commitsListFilterChanged, function() {
//            initPolling({commits: true});
//        });
//        $rootScope.$on(events.profile.emailAliasesChanged, function() {
//            initPolling({commits: true});
//        });
//        $rootScope.$on(events.commitReviewed, function() {
//            commitsCounter.decrease();
//        });
//        $rootScope.$on(events.followupDone, function() {
//            followupsCounter.decrease();
//        });
//        $rootScope.$on(events.nextCommitsLoaded, function() {
//            commitsCounter.replace();
//        });
//        $rootScope.$on(events.previousCommitsLoaded, function() {
//            commitsCounter.replace();
//        });
    }

    return {
        commitsCounter: commitsCounter,
        followupsCounter: followupsCounter,
        reloadCounters: initPolling
    }

});