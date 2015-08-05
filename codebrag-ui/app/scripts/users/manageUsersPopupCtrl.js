angular.module('codebrag.userMgmt')

    .controller('ManageUsersPopupCtrl', function($scope, userMgmtService, popupsService, Flash) {

        $scope.flash = new Flash();

        userMgmtService.loadUsers().then(function(users) {
            $scope.users = users;
        });

        $scope.countActiveUsers = function() {
            return $scope.users ? $scope.users.filter(function(user) { return user.active; }).length : 0;
        };

        $scope.invite = function() {
            popupsService.openInvitePopup();
        };

        $scope.modifyUser = function(user, flag) {
            $scope.flash.clear();
            var userData = { userId: user.userId };
            userData[flag] = user[flag];
            user.locked = true;
            userMgmtService.modifyUser(userData).then(modifySuccess, modifyFailed(flag, user)).then(function() {
                delete user.locked;
            });
        };

		$scope.deleteUser = function(user) {
		    $scope.flash.clear();
		    var userData = { userId: user.userId };		  
		    userMgmtService.deleteUser(userData).then(deleteSuccess(user), deleteFailed('active', user.userId))
		};

        $scope.askForNewPassword = function(user) {
            $scope.flash.clear();
            var modal = popupsService.openSetUserPasswordPopup(user);
            modal.result.then(function() {
                $scope.flash.add('info', 'User password changed');
            });
        };
        function deleteSuccess(user) {        	
            $scope.flash.add('error', 'User ' + user.email + ' is deleted');
            userMgmtService.loadUsers().then(function(users) {
 	            $scope.users = users;
 	        });
        }

        function modifySuccess() {
            $scope.flash.add('info', 'User details changed');
        }
	 function deleteFailed(flag, userId) {
            return function(errorsMap) {              
                $scope.flash.add('error', ' Could not delete user ');
                flattenErrorsMap(errorsMap).forEach(function(error) {
                    $scope.flash.add('error', error);
                });
            }
        }

        function modifyFailed(flag, user) {
            return function(errorsMap) {
                user[flag] = !user[flag];
                $scope.flash.add('error', 'Could not change user details');
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
