angular.module('codebrag.licence').controller('LicenceInfoCtrl', function($scope, licenceData, licenceRegistrationService, Flash) {

    $scope.licenceData = licenceData;
    $scope.flash = new Flash();

    $scope.enterKey = function() {
        $scope.enteringKey = true;
    };

    $scope.cancel = function() {
        $scope.flash.clear();
        $scope.enteringKey = null;
    };

    $scope.register = function(key) {
        $scope.flash.clear();
        licenceRegistrationService.registerKey(key).then(registrationOk, registrationFailed)
    };

    function registrationOk(licenceDetails) {
        $scope.licenceData = licenceDetails;
        $scope.enteringKey = null;
        $scope.flash.add('info', 'Your licence has been registered');
    }

    function registrationFailed(errors) {
        $scope.flash.addAll('error', errors);
    }

});