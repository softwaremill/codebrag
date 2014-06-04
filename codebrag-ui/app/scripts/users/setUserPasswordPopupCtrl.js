angular.module('codebrag.userMgmt')

    .controller('SetUserPasswordPopupCtrl', function($scope, $modalInstance, userMgmtService, Flash, user) {

        $scope.passwordData = {};
        $scope.flash = new Flash();

        $scope.setUserPassword = function(passwordData) {
            $scope.flash.clear();
            var userData = { userId: user.userId, newPass: passwordData.newPass };
            userMgmtService.modifyUser(userData).then(
                function() {
                    $modalInstance.close();
                },
                function() {
                    $scope.flash.add('error', 'Could not set user password');
                }
            );
        };

    });