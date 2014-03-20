angular.module('codebrag.commits')

    .factory('pendingCommitsListService', function(Commits, $rootScope, events, $q) {

        var pageLimit = 7;

        var nextCommits;
        var prefetchedCommitPromise;

        var commits = [];
        codebrag.commitsList.mixin.withBulkElementsManipulation.call(commits);
        codebrag.commitsList.mixin.withMarkingAsReviewed.call(commits);
        codebrag.commitsList.mixin.withIndexOperations.call(commits);

        var eventsEmitter = codebrag.commitsList.mixin.eventsEmitter($rootScope, events);

        function loadCommits() {
            return Commits.queryReviewable({limit: pageLimit}).$then(function(response) {
                commits.replaceWith(response.data.commits);
                nextCommits = response.data.newer;
                _prefetchOneMoreCommit();
                eventsEmitter.triggerAsyncCommitsCounterRefresh();
                return commits;
            });
        }

        function commitDetails(commitId) {
            return Commits.get({commitId: commitId}).$then(function(response) {
                return response.data;
            });
        }

        function markAsReviewed(commitId) {
            function removeGivenAndAppendPrefetchedCommit(prefetchedCommit) {
                var indexRemoved = commits.removeFromListBy(commitId);
                prefetchedCommit && commits.push(prefetchedCommit);
                eventsEmitter.triggerCounterDecrease();
                return commits.elementAtIndexOrLast(indexRemoved);
            }

            var commitMarkedAsReviewed =Commits.remove({commitId: commitId}).$then(function() {
                return prefetchedCommitPromise;
            });
            var nextCommitToReview = commitMarkedAsReviewed.then(removeGivenAndAppendPrefetchedCommit)
            nextCommitToReview.then(_prefetchOneMoreCommit);
            return nextCommitToReview;
        }

        function loadNextCommits() {
            if(!commits.length) return;
            var options = {min_id: commits.last().sha, limit: pageLimit, filter: 'to_review'};
            Commits.query(options).$then(function(response) {
                commits.appendAll(response.data.commits);
                nextCommits = response.data.newer;
                eventsEmitter.notifyIfNextCommitsLoaded(response.data.commits.length);
                eventsEmitter.triggerAsyncCommitsCounterRefresh();
                _prefetchOneMoreCommit();
            });
        }

        function hasNextCommits() {
            return !!nextCommits;
        }

        function hasPreviousCommits() {
            return false;
        }

        function _prefetchOneMoreCommit() {
            if(!commits.length) return;
            var options = {min_id: commits.last().sha, limit: 1, filter: 'to_review'};
            prefetchedCommitPromise = Commits.querySilent(options).$then(function(response) {
                if(!response.data.commits.length) nextCommits = 0;
                return response.data.commits.shift();
            });
        }

        return {
            loadCommits: loadCommits,
            commitDetails: commitDetails,
            loadNextCommits: loadNextCommits,
            markAsReviewed: markAsReviewed,
            hasNextCommits: hasNextCommits,
            hasPreviousCommits: hasPreviousCommits
        }

    });