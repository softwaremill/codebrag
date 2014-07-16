angular.module('codebrag.profile')

    .controller('UserProfileCtrl', function($scope, authService) {

        authService.requestCurrentUser().then(function(user) {
            $scope.user = user;
        });

    });