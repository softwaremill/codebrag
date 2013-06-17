angular.module('codebrag.commits')

    .factory('commitsListService', function($resource, $q, $http, Commits, $rootScope, commitLoadFilter, events) {

        var commits = new codebrag.AsyncCollection();

        function loadCommitsPendingReview() {
            commitLoadFilter.setPendingMode();
            return _loadCommits();
        }

        function loadAllCommits() {
            commitLoadFilter.setAllMode();
            return _loadCommits();
        }

        function loadOneMore() {
            return loadMore(1)
        }

        function loadMore(limit) {
            var request = {
                filter: commitLoadFilter.current,
                skip: commits.elements.length,
                limit: limit
            };
            var responsePromise = Commits.get(request).$then(function(response) {
                _broadcastNewCommitCountEvent(response.data.totalCount);
                return response.data.commits;
            });
            return commits.addElements(responsePromise);
        }

        function loadMoreCommits() {
            return loadMore(commitLoadFilter.maxCommitsOnList());
        }

        function _loadCommits() {
            var responsePromise = Commits.get({filter: commitLoadFilter.current}).$then(function (response) {
                _broadcastNewCommitCountEvent(response.data.totalCount);
                return response.data.commits;
            });
            return commits.loadElements(responsePromise);
        }

        function syncCommits() {
            $http({method: 'POST', url: 'rest/commits/sync'}).success(function(response) {
                commits.elements.length = 0;
                _broadcastNewCommitCountEvent(response.totalCount);
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
         * Removes commit with given identifier. Broadcasts a global event with new commit count.
         * @param commitId identifier of commit to remove.
         * @returns a promise of successful commit removal with no parameters in callback.
         */
        function removeCommit(commitId) {
            var responsePromise = Commits.remove({id: commitId}).$then();
            if (commitLoadFilter.isAll()) {
                markAsNotReviewable(commitId);
            } else {
                commits.removeElement(_matchingId(commitId), responsePromise).then(loadOneMore);
            }
            return responsePromise;
        }

        function _matchingId(id) {
            return function(element) {
                return element.id == id;
            }
        }

        /**
         * Removes commit with given identifier and returns promise of next element.
         * Broadcasts a global event with new commit count.
         * @param commitId identifier of commit to remove.
         * @returns a promise of successful commit removal. Callback function passes next available commit for review or
         * null if removed commit was last.
         */
        function removeCommitAndGetNext(commitId) {
            var responsePromise = Commits.remove({id: commitId}).$then();
            var getNextPromise;
            if (commitLoadFilter.isAll()) {
                markAsNotReviewable(commitId);
                getNextPromise = commits.getNextAfter(_matchingId(commitId), responsePromise);
            } else {
                getNextPromise = loadOneMore().then(function () {
                    return commits.removeElementAndGetNext(_matchingId(commitId), responsePromise);
                });
            }
            return getNextPromise
        }

        function loadCommitById(commitId) {
            return Commits.get({id: commitId}).$then(function(response) {
                return response.data;
            });
        }

        function _broadcastNewCommitCountEvent(totalCount) {
            $rootScope.$broadcast(events.commitCountChanged, {commitCount: totalCount})
        }

        return {
            loadCommitsPendingReview: loadCommitsPendingReview,
            loadMoreCommits: loadMoreCommits,
            loadAllCommits: loadAllCommits,
            allCommits: allCommits,
            removeCommitAndGetNext: removeCommitAndGetNext,
            removeCommit: removeCommit,
            loadCommitById: loadCommitById,
            syncCommits: syncCommits
		};

    });

