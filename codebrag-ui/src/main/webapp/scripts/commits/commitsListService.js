angular.module('codebrag.commits')

    .factory('commitsListService', function($resource, $q, $http, Commits, $rootScope, commitLoadFilter, events) {

        var commits = new codebrag.AsyncCollection();
        var totalCount = 0;

        function loadSurroundings(commitId) {
            commitLoadFilter.setAllMode();
            return $http.get('/rest/commits/' + commitId + '/context', {requestType: 'commitsList'}).then(function(response) {
                commits.replaceWith(response.data.commits);
                _broadcastNewCommitCountEvent(response.data.totalCount);
                return commits.elements;
            })
        }

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
            return Commits.query(request).$then(function(response) {
                commits.appendElements(response.data.commits);
                _broadcastNewCommitCountEvent(response.data.totalCount);
                return commits.elements;
            });
        }

        function loadMoreCommits() {
            return loadMore(commitLoadFilter.maxCommitsOnList());
        }

        function _loadCommits() {
            return Commits.query({filter: commitLoadFilter.current}).$then(function (response) {
                commits.replaceWith(response.data.commits);
                _broadcastNewCommitCountEvent(response.data.totalCount);
                return commits.elements;
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

        function _matchingId(id) {
            return function(element) {
                return element.id == id;
            }
        }

        /**
         * Removes commit with given identifier, loads one more from server (if there are more)
         * and returns promise of next element.
         * Broadcasts a global event with new commit count.
         * @param commitId identifier of commit to remove.
         * @returns a promise of successful commit removal. Callback function passes next available commit for review or
         * null if removed commit was last.
         */
        function removeCommitAndGetNext(commitId) {

            var removePromise = _removeCommitFromServer(commitId);

            if (commitLoadFilter.isAll()) {
                markAsNotReviewable(commitId);
                return commits.getNextAfter(_matchingId(commitId), removePromise);
            }
            else {
                var indexRemoved = {};
                 return commits.removeElement(_matchingId(commitId), removePromise)
                     .then(function(index) {
                         indexRemoved = index;
                     })
                     .then(_loadOneMoreIfAvailable)
                     .then(function () {
                        return commits.getElementOrNull(indexRemoved);
                    });
            }
        }

        function _removeCommitFromServer(commitId) {
            return Commits.remove({id: commitId}).$then();
        }

        function _loadOneMoreIfAvailable() {
            if (totalCount > commitLoadFilter.MAX_COMMITS_ON_LIST)
                return loadOneMore();
            else {
                totalCount--;
                _broadcastNewCommitCountEvent(totalCount);
                return $q.defer().resolve();
            }
        }

        function loadCommitById(commitId) {
            return Commits.get({id: commitId}).$then(function(response) {
                return response.data;
            });
        }

        function _broadcastNewCommitCountEvent(newTotalCount) {
            totalCount = newTotalCount;
            $rootScope.$broadcast(events.commitCountChanged, {commitCount: newTotalCount})
        }

        return {
            loadCommitsPendingReview: loadCommitsPendingReview,
            loadMoreCommits: loadMoreCommits,
            loadAllCommits: loadAllCommits,
            allCommits: allCommits,
            removeCommitAndGetNext: removeCommitAndGetNext,
            loadCommitById: loadCommitById,
            loadSurroundings: loadSurroundings
		};

    });

