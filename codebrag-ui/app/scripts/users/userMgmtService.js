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
            var dfd = $q.defer();
            $timeout(function() {
                if((userData.newPass && userData.newPass === '123') || (userData.admin === true)) {
                    dfd.reject();
                } else {
                    dfd.resolve();
                }
            }, 500);
            return dfd.promise;
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