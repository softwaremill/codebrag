angular.module('codebrag.userMgmt')

    .service('userMgmtService', function($rootScope, $http, $modal, $q, $timeout) {

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
            return $http.put(modifyUserUrl, userData);
        };

        function openPopup() {
            var config = {
                backdrop: false,
                keyboard: true,
                controller: 'ManageUsersPopupCtrl',
                templateUrl: 'views/popups/manageUsers.html'
            };
            $modal.open(config)
        }

    });