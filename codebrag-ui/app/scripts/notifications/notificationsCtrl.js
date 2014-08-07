angular.module('codebrag.notifications')

    .controller('NotificationsCtrl', function($scope, $state, $rootScope, currentCommit, notificationPoller, notificationsRegistry, currentRepoContext, events) {

        $scope.notifications = notificationsRegistry.notifications;

        currentRepoContext.ready().then(function() {
            notificationPoller.start({ markAllAsRead: true });
        });

        $scope.goToDestRepo = function(notif) {
            notificationsRegistry.markAsRead(notif);
            $scope.openCommits(notif.repo, notif.branch);
        };

        $scope.openFollowups = function() {
            $rootScope.$broadcast(events.followupsTabOpened);
            $state.transitionTo('followups.list');
        };

        $scope.openCommits = function(repo, branch) {
            currentCommit.empty();
            currentRepoContext.switchRepo(repo || currentRepoContext.repo);
            currentRepoContext.switchBranch(branch || currentRepoContext.branch);
            $rootScope.$broadcast(events.commitsTabOpened);
            $state.transitionTo('commits.list', {repo: repo || currentRepoContext.repo});
        };

        $rootScope.$on('newNotificationsAvailable', function() {
            $scope.notificationsAvailable = true;
        });

        $rootScope.$on('allNotificationsRead', function() {
            $scope.notificationsAvailable = false;
        });

    });