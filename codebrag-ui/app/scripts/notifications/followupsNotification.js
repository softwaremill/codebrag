angular.module('codebrag.notifications')

    .factory('FollowupsNotification', function(CommitsNotification) {

        var FollowupsNotification = function(count, read) {
            this.count = count;
            this.read = read || true;
        };

        FollowupsNotification.prototype = Object.create(CommitsNotification.prototype, {
            displayName: {
                value: function() {
                    return 'Followups'
                }
            }
        });

        return FollowupsNotification;

    });