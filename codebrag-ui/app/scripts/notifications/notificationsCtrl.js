angular.module('codebrag.notifications')

    .controller('NotificationsCtrl', function($scope, $state, $rootScope, currentCommit, notificationPoller, commitsNotificationsService, followupsNotificationService, currentRepoContext, events) {

        $scope.commitsNotifications = commitsNotificationsService.notifications;
        $scope.followupsNotification = followupsNotificationService.notification;

        currentRepoContext.ready().then(function() {
            notificationPoller.start();
        });

        $scope.goToDestRepo = function(notif) {
            commitsNotificationsService.markAsRead(notif);
            $scope.openCommits(notif.repo, notif.branch);
        };

        $scope.openFollowups = function() {
            followupsNotificationService.markAsRead();
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

        $rootScope.$on('commitsNotificationsAvailable', function() {
            $scope.commitsNotificationsAvailable = true;
        });

        $rootScope.$on('allCommitsNotificationsRead', function() {
            $scope.commitsNotificationsAvailable = false;
        });

        $rootScope.$on('followupsNotificationAvailable', function() {
            $scope.followupsNotificationAvailable = true;
        });

        $rootScope.$on('followupsNotificationRead', function() {
            $scope.followupsNotificationAvailable = false;
        });
        
        $scope.openDashboard = function() {
            $rootScope.$broadcast(events.allfollowupsTabOpened);
            $state.transitionTo('dashboard.list');
        };

    });
