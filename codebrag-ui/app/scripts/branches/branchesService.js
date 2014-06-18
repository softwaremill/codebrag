angular.module('codebrag.branches')

    .factory('branchesService', function($http, $q, $rootScope, events, currentRepoContext) {

        var branchesList = [],
            repositoryType,
            dataReady = $q.defer(),
            push = Array.prototype.push;

        function loadBranches() {
            var queryParams = { repo: currentRepoContext.repo };
            return $http.get('rest/branches', {params: queryParams}).then(applyBranches);
        }

        function applyBranches(response) {
            repositoryType = response.data.repoType;
            branchesList.length = 0;
            push.apply(branchesList, response.data.branches);
            dataReady.resolve();
            return branchesList;
        }

        function repoType() {
            return repositoryType;
        }

        function ready() {
            return dataReady.promise;
        }

        return {
            ready: ready,
            loadBranches: loadBranches,
            repoType: repoType
        }

    });

