angular.module('codebrag.notifications')

    .factory('repositoryStatusService', function ($rootScope, events, $modal, $http) {

        function checkRepoReadyOnLogin() {
            $rootScope.$on(events.loggedIn, function() {
                checkRepoStatus().then(displayNotReadyInfoIfRequired);
            });
        }

        function checkRepoStatus() {
            return $http.get('/rest/repoStatus').then(function(response) {
                return response.data.repoStatus;
            });
        }

        function displayModal(repoStatus) {
            var modalConfig = {
                templateUrl: 'views/repoNotReady.html',
                backdrop: false,
                keyboard: false,
                controller: 'RepositoryStatusCtrl',
                resolve: {
                    repoStatus: function () {
                        return repoStatus;
                    }
                }
            };
            $modal.open(modalConfig);
        }

        function displayNotReadyInfoIfRequired(repoStatus) {
            if(repoStatus.ready) {
               return;
            }
            displayModal(repoStatus);
        }

        return {
            checkRepoReadyOnLogin: checkRepoReadyOnLogin
        }

    });