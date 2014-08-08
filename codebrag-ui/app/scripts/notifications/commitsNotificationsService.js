angular.module('codebrag.notifications')

    .service('commitsNotificationsService', function($rootScope) {

        var notifications = [];

        this.notifications = notifications;

        this.mergeAll = function(notifs) {
            var merge = this.merge.bind(this);
            cleanupStale(notifs);
            notifs.forEach(merge);
            if(this.notificationsAvailable()) {
                $rootScope.$emit('commitsNotificationsAvailable');
            }
        };

        this.merge = function(notif) {
            var found = this.find(notif);
            if(found) {
                found.updateCount(notif);
            } else {
                notifications.push(notif);
            }
        };

        this.find = function(notif) {
            return _.find(notifications, identity(notif));
        };

        this.markAsRead = function(notif) {
            var found = this.find(notif);
            if(found) {
                found.read = true;
            }
            if(!this.notificationsAvailable()) {
                $rootScope.$emit('allCommitsNotificationsRead');
            }
        };

        this.markAllAsRead = function() {
            var markAsRead = this.markAsRead.bind(this);
            this.notifications.forEach(markAsRead);
        };

        this.notificationsAvailable = function() {
            var unread = notifications.filter(function(n) {
                return n.active() && !n.read;
            });
            return unread.length > 0;
        };

        function cleanupStale(incoming) {
            var stale = _.difference(notifications, incoming);
            stale.forEach(function(toRemove) {
                var index = notifications.indexOf(toRemove);
                notifications.splice(index, 1);
            });
        }

        function identity(notif) {
            return function(e) {
                return e.branch === notif.branch && e.repo === notif.repo;
            }
        }
    });