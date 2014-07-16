angular.module('codebrag.profile')

    .controller('UserSettingsCtrl', function($scope, configService, userSettingsService, $q, Flash) {

        $scope.flash = new Flash();

        loadCurrentUserNotificationSettingsIfGlobalEnabled();

        $scope.notificationsChanged = function() {
            $scope.flash.clear();
            toggleActionPendingFlag(true);
            userSettingsService.save($scope.userSettings).then(success, error).then(function() {
                toggleActionPendingFlag(false);
            });
            function success() {
                $scope.flash.add('info', 'Notification settings changed');
            }
            function error() {
                $scope.flash.add('error', 'Unable to change notification settings');
            }
        };

        function loadCurrentUserNotificationSettingsIfGlobalEnabled() {
            configService.fetchConfig().then(function (appConfig) {
                return appConfig.emailNotifications ? userSettingsService.load() : $q.reject();
            }).then(function (userSettings) {
                $scope.userSettings = userSettings;
            });
        }

        function toggleActionPendingFlag(value) {
            if(angular.isUndefined(value)) {
                $scope.actionPending = !$scope.actionPending;
            } else {
                $scope.actionPending = value;
            }
        }

    });