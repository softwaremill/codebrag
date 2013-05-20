angular.module('codebrag.commits')

    .factory('commitsListService', function($resource, $q, $http, Commits, commitLoadFilter, notificationCountersService) {

        var commits = new codebrag.AsyncCollection();


    	function loadCommitsFromServer(filter) {
            var responsePromise = Commits.get({filter: filter.value}).$then(function(response) {
                notificationCountersService.updateCommits(_reviewableCount(response.data.commits));
                return response.data.commits;
            });
            return commits.loadElements(responsePromise);
    	}

        function syncCommits() {
            $http({method: 'POST', url: 'rest/commits/sync'}).success(function(response) {
                commits.elements.length = 0;
                notificationCountersService.updateCommits(_reviewableCount(response.commits));
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
            notificationCountersService.decreaseCommits();
            var responsePromise = Commits.remove({id: commitId}).$then();
            if (commitLoadFilter.isAll()) {
                markAsNotReviewable(commitId);
                return responsePromise;
            }
            else
                return commits.removeElement(_matchingId(commitId), responsePromise);
        }

        function _matchingId(id) {
            return function(element) {
                return element.id == id;
            }
        }

        function removeCommitAndGetNext(commitId) {
            notificationCountersService.decreaseCommits();
            var responsePromise = Commits.remove({id: commitId}).$then();
                if (commitLoadFilter.isAll()) {
                    markAsNotReviewable(commitId);
                    return commits.getNextAfter(_matchingId(commitId), responsePromise);
                }
                else
                    return commits.removeElementAndGetNext(_matchingId(commitId), responsePromise);
        }

        function loadCommitById(commitId) {
            return Commits.get({id: commitId}).$then(function(response) {
                return response.data;
            });
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

