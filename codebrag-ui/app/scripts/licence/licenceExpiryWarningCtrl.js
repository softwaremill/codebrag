angular.module('codebrag.licence').controller('LicenceExpiryWarningCtrl', function($scope, licenceService, events) {

    licenceService.ready().then(function() {
        $scope.licenceData = licenceService.getLicenceData();
    });

    $scope.$on(events.licence.licenceKeyRegistered, function() {
        $scope.visible = false;
    });

    $scope.$on(events.licence.licenceAboutToExpire, function() {
        $scope.visible = true;
        $scope.licenceData = licenceService.getLicenceData();
    });

    $scope.$on(events.licence.licenceExpired, function() {
        $scope.visible = true;
        $scope.licenceData = licenceService.getLicenceData();
    });

    $scope.displayPopup = licenceService.licencePopup;

});