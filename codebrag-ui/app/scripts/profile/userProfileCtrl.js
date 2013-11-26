angular.module('codebrag.profile')

    .controller('UserProfileCtrl', function($scope, authService, configService, userSettingsService, $q) {

        loadCurrentUserData();
        loadCurrentUserNotificationSettingsIfGlobalEnabled();

        $scope.notificationsChanged = function() {
            $scope.savingStatus = 'pending';
            userSettingsService.save($scope.userSettings).then(success, error);

            function success() {
                $scope.savingStatus = 'success';
            }
            function error() {
                $scope.savingStatus = 'failed';
            }
        };

        $scope.isSavePending = function() {
            return $scope.savingStatus && $scope.savingStatus == 'pending';
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