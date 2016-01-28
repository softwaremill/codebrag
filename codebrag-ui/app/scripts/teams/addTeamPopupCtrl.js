angular.module('codebrag.teamMgmt')

    .controller('AddTeamPopupCtrl', function($scope, $modalInstance, Flash, teamMgmtService) {

        $scope.flash = new Flash();

        $scope.addTeam = function(name) {
            $scope.flash.clear();
            teamMgmtService.createTeam(name).then(
                function(createdTeam) {
                    $modalInstance.close(createdTeam);
                },
                function(errors) {
                    $scope.flash.addAll('error', errors);
                }
            );
        };

    });