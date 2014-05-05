angular.module('codebrag.licence').service('registrationStatus', function() {
    var proto = {
        ok: null,
            errMsg: null,
        setOk: function() { this.ok = true },
        setFailed: function(msg) {
            this.ok = false;
            this.errMsg = msg;
        },
        isOk: function() { return this.ok === true },
        isFailed: function() { return !this.isOk() }
    };

    this.createNew = function() {
        return Object.create(proto);
    }
});

angular.module('codebrag.licence').controller('LicenceInfoCtrl', function($scope, licenceData, licenceRegistrationService, registrationStatus) {

    $scope.licenceData = licenceData;
    $scope.registrationStatus = null;

    $scope.enterKey = function() {
        $scope.enteringKey = true;
    };

    $scope.cancel = function() {
        $scope.enteringKey = null;
        $scope.registrationStatus = null;
    };

    $scope.register = function(key) {
        $scope.registrationStatus = registrationStatus.createNew();
        licenceRegistrationService.registerKey(key).then(registrationOk, registrationFailed)
    };

    function registrationOk(licenceDetails) {
        $scope.licenceData = licenceDetails;
        $scope.enteringKey = null;
        $scope.registrationStatus.setOk();
    }

    function registrationFailed(errorMsg) {
        $scope.registrationStatus.setFailed(errorMsg);
    }

});