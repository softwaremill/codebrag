angular.module('codebrag.session')

    .controller("ProfileCtrl", function ProfileCtrl($scope, authService) {
        $scope.user = authService.loggedInUser;
    });