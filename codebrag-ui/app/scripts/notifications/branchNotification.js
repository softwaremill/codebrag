angular.module('codebrag.notifications')

    .factory('FollowupsNotification', function(BranchNotification) {

        var FollowupsNotification = function(count, read) {
            this.count = count;
            this.read = read || true;
        };

        FollowupsNotification.prototype = Object.create(BranchNotification.prototype, {
            displayName: {
                value: function() {
                    return 'Followups'
                }
            }
        });

        return FollowupsNotification;

    })

/*
Single element of popup with notifications list (repo, branch, count).
"Active" - when there are commits for this branch
"Read" - when notifications popup was opened and notifications were read
*/

    .factory('BranchNotification', function() {

        var BranchNotification = function(repo, branch, count, read) {
            this.repo = repo;
            this.branch = branch;
            this.count = count || 0;
            this.read = read || true;
        };

        BranchNotification.prototype = {
            updateCount: function(newNotif) {
                if(newNotif.count > this.count) {
                    this.read = false;
                }
                this.count = newNotif.count;
            },
            active: function() {
                return this.count != 0;
            },
            displayName: function() {
                return [this.repo, '/', this.branch].join('');
            }
        };

        return BranchNotification;

    });
