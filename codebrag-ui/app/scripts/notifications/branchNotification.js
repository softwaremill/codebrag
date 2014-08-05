angular.module('codebrag.notifications')

/*
Single element of popup with notifications list (repo, branch, count).
"Active" - when there are commits for this branch
"Read" - when notifications popup was opened and notifications were read
*/

    .factory('BranchNotification', function() {

        var BranchNotification = function(repo, branch, commitsCount, read) {
            this.repo = repo;
            this.branch = branch;
            this.commitsCount = commitsCount || 0;
            this.read = read || true;
        };

        BranchNotification.prototype = {
            updateCount: function(newCount) {
                if(newCount > this.commitsCount) {
                    this.read = false;
                }
                this.commitsCount = newCount;
            },
            active: function() {
                return this.commitsCount != 0;
            }
        };

        return BranchNotification;

    });
