angular.module('codebrag.branches')

    .factory('branchesService', function($http, $q, $rootScope, currentRepoContext, RepoBranch) {

        var branches = [],
            repositoryType,
            dataReady = $q.defer(),
            push = Array.prototype.push;

        function baseUrl() {
            return 'rest/repos/' + currentRepoContext.repo + '/branches'
        }

        function toggleWatching(branch) {
            var original = angular.copy(branch),
                url = baseUrl() + '/' + branch.name + '/watch',
                httpCall = (branch.watching ? $http.delete(url) : $http.post(url));
            branch.watching = !branch.watching;
            return httpCall.then(null, function() {
                // revert back to original state when error
                branch.watching = original.watching;
            });
        }

        function loadBranches() {
            return $http.get(baseUrl()).then(applyBranches);
        }

        function applyBranches(response) {
            repositoryType = response.data.repoType;
            branches.length = 0;
            push.apply(branches, toBranches(response.data.branches));
            dataReady.resolve();
        }

        function toBranches(arr) {
            return arr.map(function(branchData) {
                return new RepoBranch(branchData);
            });
        }

        function loadCurrentBranchCommitsCount() {
            return $http.get(baseUrl() + '/' + currentRepoContext.branch + '/count').then(function(resp) {
                return resp.data.toReviewCount;
            });
        }

        function repoType() {
            return repositoryType;
        }

        function ready() {
            return dataReady.promise;
        }

        return {
            branches: branches,
            ready: ready,
            loadBranches: loadBranches,
            toggleWatching: toggleWatching,
            loadCurrentBranchCommitsCount: loadCurrentBranchCommitsCount,
            repoType: repoType
        }

    });

