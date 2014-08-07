angular.module('codebrag.notifications')

    .factory('followupsNotificationService', function($rootScope, FollowupsNotification) {

        var notification = new FollowupsNotification(0),
            initialUpdate = true;

        function merge(notif) {
            notification.updateCount(notif);
            if(initialUpdate) {
                notification.read = true;
                initialUpdate = false;
            }
            if(notificationAvailable()) {
                $rootScope.$emit('followupsNotificationAvailable');
            }
        }

        function markAsRead() {
            notification.read = true;
            $rootScope.$emit('followupsNotificationRead');
        }

        function notificationAvailable() {
            return !notification.read;
        }

        return {
            notification: notification,
            merge: merge,
            markAsRead: markAsRead
        }

    });