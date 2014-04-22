angular.module('codebrag.licence').controller('LicenceExpiryWarningCtrl', function($scope, licenceService) {

    licenceService.ready().then(function() {
        $scope.licenceData = licenceService.getLicenceData();
    });

    $scope.$on('codebrag:licenceAboutToExpire', function() {
        $scope.visible = true;
    });

    $scope.$on('codebrag:licenceExpired', function() {
        $scope.visible = true;
    });

    $scope.displayPopup = licenceService.licencePopup;

});