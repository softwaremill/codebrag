angular.module('codebrag.commits')

    .factory('commitsListService', function($resource, $q, $http, Commits, commitLoadFilter, $rootScope) {

        var commits = new codebrag.AsyncCollection();

    	function loadCommitsFromServer(filter) {
            var responsePromise = Commits.get({filter: filter.value}).$then(function(response) {
                var newCommitCount = _reviewableCount(response.data.commits);
                _broadcastNewCommitCountEvent(newCommitCount);
                return response.data.commits;
            });
            return commits.loadElements(responsePromise);
    	}

        function syncCommits() {
            $http({method: 'POST', url: 'rest/commits/sync'}).success(function(response) {
                commits.elements.length = 0;
                var newCommitCount = _reviewableCount(response.commits);
                _broadcastNewCommitCountEvent(newCommitCount);
                _.forEach(response.commits, function(commit) {
                    commits.elements.push(commit);
                });
            });
        }

        function allCommits() {
            return commits.elements;
        }

        function markAsNotReviewable(commitId) {
            commits.elements.some(function (commit) {
                if (commit.id == commitId) {
                    commit.pendingReview = false;
                    return true;
                }
                return false;
            });
        }

        /**
         * Removes commit with given identifier.
         * @param commitId identifier of commit to remove.
         * @returns a promise
         */
        function removeCommit(commitId) {
            var responsePromise = Commits.remove({id: commitId}).$then();
            var resultPromise = undefined;
            if (commitLoadFilter.isAll()) {
                markAsNotReviewable(commitId);
                resultPromise = responsePromise;
            }
            else
                resultPromise = commits.removeElement(_matchingId(commitId), responsePromise);
            return resultPromise.then(function (next) {
                _broadcastNewCommitCountEvent(commits.elements.length - 1);
                return next;
            })
        }

        function _matchingId(id) {
            return function(element) {
                return element.id == id;
            }
        }

        function removeCommitAndGetNext(commitId) {
            var responsePromise = Commits.remove({id: commitId}).$then();
            var resultPromise = undefined;
            if (commitLoadFilter.isAll()) {
                markAsNotReviewable(commitId);
                resultPromise = commits.getNextAfter(_matchingId(commitId), responsePromise);
            }
            else
                resultPromise = commits.removeElementAndGetNext(_matchingId(commitId), responsePromise);
            return resultPromise.then(function (next) {
                _broadcastNewCommitCountEvent(commits.elements.length - 1);
                return next;
            })
        }

        function loadCommitById(commitId) {
            return Commits.get({id: commitId}).$then(function(response) {
                return response.data;
            });
        }

        function _broadcastNewCommitCountEvent(newCommitCount) {
            $rootScope.$broadcast("codebrag:commitCountChanged", {commitCount: newCommitCount})
        }

        function _reviewableCount(commits) {
            return _.filter(commits, function (commit) {
                return commit.pendingReview
            }).length
        }

        return {
            loadCommitsFromServer: loadCommitsFromServer,
            allCommits: allCommits,
            removeCommitAndGetNext: removeCommitAndGetNext,
            removeCommit: removeCommit,
            loadCommitById: loadCommitById,
            syncCommits: syncCommits
		};

    });

