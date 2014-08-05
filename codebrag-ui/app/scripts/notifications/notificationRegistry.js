angular.module('codebrag.notifications')

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

        this.notificationsAvailable = function() {
            var unread = notifications.filter(function(n) {
                return !n.read
            });
            return unread.length > 0;
        };

        function identity(repo, branch) {
            return function(e) {
                return (e.repo === repo && e.branch === branch);
            }
        }

    });