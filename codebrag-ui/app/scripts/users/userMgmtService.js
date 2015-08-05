angular.module('codebrag.userMgmt')

    .service('userMgmtService', function($rootScope, $http, $modal, $q) {

        var usersApiUrl = 'rest/users';

        this.initialize = function() {
            $rootScope.$on('openUserMgmtPopup', openPopup);
        };

        this.loadUsers = function() {
            return $http.get(usersApiUrl).then(function(response) {
                return response.data.users;
            });
        };

        this.modifyUser = function(userData) {
            var modifyUserUrl = [usersApiUrl, '/', userData.userId].join('');
            return $http.put(modifyUserUrl, userData).then(null, modifyUserFailed);
        };

        this.deleteUser = function(userData) {
			var deleteUserUrl = [usersApiUrl, '/', userData.userId].join('');
			return $http.delete(deleteUserUrl).then(null, modifyUserFailed);
        };

        function modifyUserFailed(response) {
            return $q.reject(response.data);
        }

        function openPopup() {
            var config = {
                backdrop: false,
                keyboard: true,
                controller: 'ManageUsersPopupCtrl',
                templateUrl: 'views/popups/manageUsers.html',
                windowClass: 'manage-users'
            };
            $modal.open(config)
        }

    });
