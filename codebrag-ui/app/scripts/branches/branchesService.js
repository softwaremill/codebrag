angular.module('codebrag.branches')

    .factory('branchesService', function($http, $q, $rootScope, RepoBranch, events) {

        var branches = [],
            repositoryType,
            dataReady = $q.defer(),
            push = Array.prototype.push;

        var urls = {
            getRepoBranches: function(repoName) {
                return 'rest/repos/' + repoName + '/branches'
            },
            toggleWatchBranch: function(repoName, branchName) {
                return this.getRepoBranches(repoName) + '/' + branchName + '/watch'
            },
            currentBranchCommitsCount: function(repoName, branchName) {
                return this.getRepoBranches(repoName) + '/' + branchName + '/count'
            }
        };

        function toggleWatching(repoName, branch) {
            var original = angular.copy(branch),
                url = urls.toggleWatchBranch(repoName, branch.name);
                httpCall = (branch.watching ? $http.delete(url) : $http.post(url));
            branch.watching = !branch.watching;
            return httpCall.then(null, function() {
                // revert back to original state when error
                branch.watching = original.watching;
                return $q.reject();
            }).then(function() {
                $rootScope.$broadcast(events.branches.branchWatchToggle);
            });
        }

        function loadBranches(repoName) {
            var url = urls.getRepoBranches(repoName);
            return $http.get(url).then(applyBranches);
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

        function loadBranchCommitsToReviewCount(repoName, branchName) {
            var url = urls.currentBranchCommitsCount(repoName, branchName);
            return $http.get(url).then(function(resp) {
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
            loadBranchCommitsToReviewCount: loadBranchCommitsToReviewCount,
            repoType: repoType
        }

    });

