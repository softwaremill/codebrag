angular.module('codebrag.branches')

    .factory('branchesService', function($http, $q, $rootScope, currentRepoContext, RepoBranch) {

        var branchesList = [],
            repositoryType,
            dataReady = $q.defer(),
            push = Array.prototype.push;

        function loadBranches() {
            return $http.get('rest/repos/' + currentRepoContext.repo + '/branches').then(applyBranches);
        }

        function applyBranches(response) {
            repositoryType = response.data.repoType;
            branchesList.length = 0;
            push.apply(branchesList, toBranches(response.data.branches));
            dataReady.resolve();
            return branchesList;
        }

        function toBranches(arr) {
            return arr.map(function(branchData) {
                return new RepoBranch(branchData);
            });
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

