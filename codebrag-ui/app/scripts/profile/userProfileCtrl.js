angular.module('codebrag.profile')

    .controller('UserProfileCtrl', function($scope, authService, configService, userSettingsService, $q) {

        $scope.user = authService.loggedInUser;

        configService.fetchConfig().then(function(appConfig) {
            return appConfig.emailNotifications ? userSettingsService.load() : $q.reject();
        }).then(function(userSettings) {
            $scope.userSettings = userSettings;
        });

        $scope.notificationsChanged = function() {
            $scope.savingStatus = new codebrag.OperationStatus();
            $scope.savingStatus.setPending();
            userSettingsService.save($scope.userSettings).then(success, error);

            function success() {
                $scope.savingStatus.setOk();
            }
            function error() {
                $scope.savingStatus.setErr();
            }
        };

    });
