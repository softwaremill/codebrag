angular.module('codebrag.notifications')

    .controller('NotificationsCtrl', function($scope, $state, $rootScope, currentCommit, notificationPoller, notificationsRegistry, currentRepoContext, events) {

        $scope.notifications = notificationsRegistry.notifications;

        currentRepoContext.ready().then(function() {
            notificationPoller.start({ markAllAsRead: true });
        });

        $scope.goToDestRepo = function(notif) {
            currentCommit.empty();
            currentRepoContext.switchRepo(notif.repo);
            currentRepoContext.switchBranch(notif.branch);
            notificationsRegistry.markAsRead(notif);
            $rootScope.$broadcast(events.commitsTabOpened);
            $state.transitionTo('commits.list', {repo: notif.repo});
        };

        $scope.openFollowups = function() {
            $rootScope.$broadcast(events.followupsTabOpened);
            $state.transitionTo('followups.list');
        };


        $scope.openNotificationsPopup = function() {
            $rootScope.$broadcast('openNotificationsPopup');
        };

        $rootScope.$on('newNotificationsAvailable', function() {
            $scope.notificationsAvailable = true;
        });

        $rootScope.$on('allNotificationsRead', function() {
            $scope.notificationsAvailable = false;
        });

    });