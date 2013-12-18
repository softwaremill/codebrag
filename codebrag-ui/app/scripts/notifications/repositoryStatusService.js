angular.module('codebrag.notifications')

    .factory('repositoryStatusService', function ($http, $q, popupsService) {

        function checkRepoReady() {
            return getRepoState().then(function(repoState) {
                displayNotReadyInfoIfRequired(repoState);
                return repoState.ready ? $q.when(repoState) : $q.reject(repoState);
            });
        }

        function getRepoState() {
            return $http.get('rest/repoStatus').then(function(response) {
                return response.data.repoStatus;
            });
        }

        function displayNotReadyInfoIfRequired(repoStatus) {
            if(repoStatus.ready) {
               return;
            }
            popupsService.openRepoNotReadyPopup(repoStatus);
        }

        return {
            checkRepoReady: checkRepoReady
        }

    });