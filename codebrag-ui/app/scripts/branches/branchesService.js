angular.module('codebrag.branches')

    .factory('branchesService', function($http, $q, $rootScope, RepoBranch, events) {

        var branches = [],
            repositoryType,
            dataReady = $q.defer(),
            push = Array.prototype.push;

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

        return {

            branches: branches,

            urls: {
                getRepoBranches: function (repoName) {
                    return 'rest/repos/' + repoName + '/branches'
                },
                toggleWatchBranch: function (repoName, branchName) {
                    return this.getRepoBranches(repoName) + '/' + branchName + '/watch'
                },
                currentBranchCommitsCount: function (repoName, branchName) {
                    return this.getRepoBranches(repoName) + '/' + branchName + '/count'
                }
            },

            toggleWatching: function (repoName, branch) {
                var original = angular.copy(branch),
                    url = this.urls.toggleWatchBranch(repoName, branch.name);
                httpCall = (branch.watching ? $http.delete(url) : $http.post(url));
                branch.watching = !branch.watching;
                return httpCall.then(null, function () {
                    // revert back to original state when error
                    branch.watching = original.watching;
                    return $q.reject();
                }).then(function () {
                    $rootScope.$broadcast(events.branches.branchWatchToggle);
                });
            },

            loadBranches: function (repoName) {
                var url = this.urls.getRepoBranches(repoName);
                return $http.get(url).then(applyBranches);
            },

            loadBranchCommitsToReviewCount: function (repoName, branchName) {
                var url = this.urls.currentBranchCommitsCount(repoName, branchName);
                return $http.get(url).then(function (resp) {
                    return resp.data.toReviewCount;
                });
            },

            repoType: function () {
                return repositoryType;
            },

            ready: function ready() {
                return dataReady.promise;
            }

        }
    });

