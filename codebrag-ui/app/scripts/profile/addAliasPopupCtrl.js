angular.module('codebrag.profile')

    .controller('AddAliasPopupCtrl', function($scope, $modalInstance, Flash, emailAliasesService) {

        $scope.flash = new Flash();

        $scope.addAlias = function(email) {
            $scope.flash.clear();
            emailAliasesService.createAlias(email).then(
                function(createdAlias) {
                    $modalInstance.close(createdAlias);
                },
                function(errors) {
                    $scope.flash.addAll('error', errors);
                }
            );
        };

    });