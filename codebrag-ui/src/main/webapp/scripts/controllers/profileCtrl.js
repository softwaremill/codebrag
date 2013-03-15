angular.module('smlCodebrag.profile').controller("ProfileCtrl", function ProfileCtrl($scope, UserSessionService) {
    $scope.login = UserSessionService.loggedUser.login.concat();
});