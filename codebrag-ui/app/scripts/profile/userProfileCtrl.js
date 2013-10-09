angular.module('codebrag.profile')

    .controller('UserProfileCtrl', function($scope, authService, configService, userSettingsService, $q) {

        $scope.savingStatus = new codebrag.OperationStatus();

        loadCurrentUserData();
        loadCurrentUserNotificationSettingsIfGlobalEnabled();

        $scope.notificationsChanged = function() {
            $scope.savingStatus.setPending();
            userSettingsService.save($scope.userSettings).then(success, error);

            function success() {
                $scope.savingStatus.setOk();
            }
            function error() {
                $scope.savingStatus.setErr();
            }
        };

        function loadCurrentUserData() {
            authService.requestCurrentUser().then(function (user) {
                $scope.user = user;
            });
        }

        function loadCurrentUserNotificationSettingsIfGlobalEnabled() {
            configService.fetchConfig().then(function (appConfig) {
                return appConfig.emailNotifications ? userSettingsService.load() : $q.reject();
            }).then(function (userSettings) {
                $scope.userSettings = userSettings;
            });
        }

    });
