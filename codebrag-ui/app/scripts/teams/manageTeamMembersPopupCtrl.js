angular.module('codebrag.teamMgmt')

    .controller('ManageTeamMembersPopupCtrl', function($scope, teamMgmtService, Flash, team) {
        $scope.flash = new Flash();

        teamMgmtService.loadUsers(team).then(function(users) {
            $scope.users = users;
        });

        $scope.modifyTeamMember = function(user, flag) {
            $scope.flash.clear();
            var teamData = { teamId: user.teamId, userId: user.userId };
            teamData[flag] = user[flag];
            user.locked = true;
            if (flag == 'member') {
            	if (user[flag]) {
                    teamMgmtService.addTeamMember(teamData).then(modifySuccess, modifyFailed(flag, user)).then(function() {
                        delete user.locked;
                        user.contributor = true;
                    });
            	} else {
                    teamMgmtService.deleteTeamMember(teamData).then(modifySuccess, modifyFailed(flag, user)).then(function() {
                        delete user.locked;
                        user.contributor = false;
                    });
            	}
            } else {
                teamMgmtService.modifyTeamMember(teamData).then(modifySuccess, modifyFailed(flag, user)).then(function() {
                    delete user.locked;
                });
            }
        };

        function modifySuccess() { 
            $scope.flash.add('info', 'Member details changed');
        }

        function modifyFailed(flag, user) {
            return function(errorsMap) {
            	if (flag != null) {
            		user[flag] = !user[flag];
            	}
                $scope.flash.add('error', 'Could not update team member');
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
