angular.module('codebrag.notifications')

    .factory('notificationPoller', function($http, $timeout, $rootScope, CommitsNotification, FollowupsNotification, commitsNotificationsService, followupsNotificationService, events) {

        var timer,
            delay = 30000;

        bindEventListeners();

        function call() {
            fetchNotifications().then(function(notifs) {
                commitsNotificationsService.mergeAll(notifs.commits);
                followupsNotificationService.merge(notifs.followups);
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
            return {
                commits: resp.data.repos.map(function(c) {
                    return new CommitsNotification(c.repoName, c.branchName, c.commits);
                }),
                followups: new FollowupsNotification(resp.data.followups)
            };
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
