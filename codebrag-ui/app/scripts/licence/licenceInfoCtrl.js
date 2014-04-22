angular.module('codebrag.licence').controller('LicenceInfoCtrl', function($scope, licenceService) {

    $scope.licenceData = licenceService.getLicenceData();

});