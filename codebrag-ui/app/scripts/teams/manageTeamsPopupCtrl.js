angular.module('codebrag.teamMgmt')

    .controller('ManageTeamsPopupCtrl', function($scope, teamMgmtService, popupsService, Flash) {
        $scope.flash = new Flash();

        teamMgmtService.loadTeams().then(function(teams) {
            $scope.teams = teams;
        });

        $scope.add = function() {
            popupsService.openAddTeamPopup();
        };
        
        $scope.deleteTeam = function(team) {
            $scope.flash.clear();
            var teamData = { teamId: team.id };
            team.locked = true;
            teamMgmtService.deleteTeam(teamData).then(deleteSuccess, deleteFailed(team)).then(function() {
                delete team.locked; 
            });
        }
        
        $scope.manageMembers = function(team) {
        	popupsService.openManageTeamMembersPopup(team);
        };
        
        function deleteSuccess() {
            $scope.flash.add('info', 'Team deleted');
        }

        function deleteFailed(team) {
            return function(errorsMap) {
                $scope.flash.add('error', 'Could not delete team ' + team.name);
                flattenErrorsMap(errorsMap).forEach(function(error) {
                    $scope.flash.add('error', error);
                });
            }
        }

        function flattenErrorsMap(errorsMap) {
            var nestedErrorsList = Object.keys(errorsMap).map(function(key) {
                return errorsMap[key];
            });
            return _.flatten(nestedErrorsList)
        }
    });
