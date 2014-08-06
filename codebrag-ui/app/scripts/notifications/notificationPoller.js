angular.module('codebrag.notifications')

    .factory('notificationPoller', function($http, $timeout, $rootScope, BranchNotification, FollowupsNotification, notificationsRegistry, events) {

        var timer,
            delay = 30000;

        bindEventListeners();

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
            if(timer) {
                $timeout.cancel(timer);
                timer = null;
                console.log('Notification poller stopped');
            }
        }

        function restart() {
            stop();
            start();
        }

        function fetchNotifications() {
            return $http.get('rest/notifications').then(toNotifications);
        }

        function toNotifications(resp) {
            var notifs =  resp.data.repos.map(function(c) {
                return new BranchNotification(c.repoName, c.branchName, c.commits);
            });
            notifs.push(new FollowupsNotification(resp.data.followups));
            return notifs;
        }

        function bindEventListeners() {
            $rootScope.$on(events.commitReviewed, restart);
            $rootScope.$on(events.followupDone, restart);
        }

        return {
            start: start,
            stop: stop,
            restart: restart
        };

    });
