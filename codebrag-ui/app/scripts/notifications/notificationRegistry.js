angular.module('codebrag.notifications')

    .service('notificationsRegistry', function($rootScope) {

        var notifications = [];

        this.notifications = notifications;

        this.mergeAll = function(notifs) {
            var merge = this.merge.bind(this);
            notifs.forEach(merge);
            if(this.notificationsAvailable()) {
                $rootScope.$emit('newNotificationsAvailable');
            }
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

        this.markAsRead = function(notif) {
            var found = this.find(notif.repo, notif.branch);
            if(found) {
                found.read = true;
            }
            if(!this.notificationsAvailable()) {
                $rootScope.$emit('allNotificationsRead');
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

        function identity(repo, branch) {
            return function(e) {
                return (e.repo === repo && e.branch === branch);
            }
        }

    });