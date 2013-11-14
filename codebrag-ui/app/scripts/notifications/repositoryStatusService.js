angular.module('codebrag.notifications')

    .factory('repositoryStatusService', function ($modal, $http, $q) {

        function checkRepoReady() {
            return getRepoState().then(function(repoState) {
                displayNotReadyInfoIfRequired(repoState);
                return repoState.ready ? $q.when(repoState) : $q.reject(repoState);
            });
        }

        function getRepoState() {
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
            checkRepoReady: checkRepoReady
        }

    });