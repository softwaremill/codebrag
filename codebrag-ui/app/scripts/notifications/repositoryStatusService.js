angular.module('codebrag.notifications')

    .factory('repositoryStatusService', function ($rootScope, events, $modal, $q) {

        function checkRepoReadyOnLogin() {
            $rootScope.$on(events.loggedIn, function() {
                checkRepoStatus().then(displayNotReadyInfoIfRequired);
            });
        }

        // TODO: replace with $http call
        function checkRepoStatus() {
            var result = {
                ready: true
            };
            return $q.when(result);
        }

        function displayModal(statusData) {
            var modalConfig = {
                templateUrl: 'views/repoNotReady.html',
                backdrop: false,
                keyboard: false,
                controller: 'RepositoryStatusCtrl',
                resolve: {
                    statusData: function () {
                        return statusData;
                    }
                }
            };
            $modal.open(modalConfig);
        }

        function displayNotReadyInfoIfRequired(statusData) {
            if(statusData.ready) {
               return;
            }
            displayModal(statusData);
        }

        return {
            checkRepoReadyOnLogin: checkRepoReadyOnLogin
        }

    });