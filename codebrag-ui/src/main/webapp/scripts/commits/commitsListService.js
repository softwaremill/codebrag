angular.module('codebrag.commits')

    .factory('commitsListService', function($resource, $q, $http, Commits, $rootScope) {

        var commits = new codebrag.AsyncCollection();

        var commitsFilter = {
            ALL_COMMITS: 'all',
            PENDING_COMMITS: 'pending',
            current: this.PENDING_COMMITS,

            isEnabled: function() {
                return this.current === this.PENDING_COMMITS;
            },

            disableFilter: function() {
                this.current = this.ALL_COMMITS;
            },

            enableFilter: function() {
                this.current = this.PENDING_COMMITS;
            }

        };

        function loadCommitsPendingReview() {
            commitsFilter.enableFilter();
            return _loadCommits();
        }

        function loadAllCommits() {
            commitsFilter.disableFilter();
            return _loadCommits();
        }

        function _loadCommits() {
            var responsePromise = Commits.get({filter: commitsFilter.current}).$then(function(response) {
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
         * Removes commit with given identifier. Broadcasts a global event with new commit count.
         * @param commitId identifier of commit to remove.
         * @returns a promise of successful commit removal with no parameters in callback.
         */
        function removeCommit(commitId) {
            var responsePromise = Commits.remove({id: commitId}).$then();
            responsePromise.then(function (next) {
                _broadcastNewCommitCountEvent(commits.elements.length - 1);
                return next;
            });
            if (!commitsFilter.isEnabled()) {
                markAsNotReviewable(commitId);
            } else {
                commits.removeElement(_matchingId(commitId), responsePromise);
            }
            return responsePromise
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
            responsePromise.then(function (next) {
                _broadcastNewCommitCountEvent(commits.elements.length - 1);
                return next;
            });
            var getNextPromise;
            if (!commitsFilter.isEnabled()) {
                markAsNotReviewable(commitId);
                getNextPromise = commits.getNextAfter(_matchingId(commitId), responsePromise);
            } else {
                getNextPromise = commits.removeElementAndGetNext(_matchingId(commitId), responsePromise);
            }
            return getNextPromise
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
            loadCommitsPendingReview: loadCommitsPendingReview,
            loadAllCommits: loadAllCommits,
            allCommits: allCommits,
            removeCommitAndGetNext: removeCommitAndGetNext,
            removeCommit: removeCommit,
            loadCommitById: loadCommitById,
            syncCommits: syncCommits
		};

    });

