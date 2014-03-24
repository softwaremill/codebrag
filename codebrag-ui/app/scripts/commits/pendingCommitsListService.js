angular.module('codebrag.commits')

    .factory('pendingCommitsListService', function(Commits, $rootScope, events, branchesService) {

        var self = this;

        var pageLimit = 7;

        var nextCommits;
        var prefetchedCommitPromise;

        var commits = [];
        codebrag.commitsList.mixin.withBulkElementsManipulation.call(commits);
        codebrag.commitsList.mixin.withMarkingAsReviewed.call(commits);
        codebrag.commitsList.mixin.withIndexOperations.call(commits);

        codebrag.commitsList.mixin.urlParams(this);

        var eventsEmitter = codebrag.commitsList.mixin.eventsEmitter($rootScope, events);

        function loadCommits() {
            var options = {};
            options[self.urlParams.limit] = pageLimit;
            options[self.urlParams.branch] = branchesService.selectedBranch();
            return Commits.queryReviewable(options).$then(function(response) {
                commits.replaceWith(response.data.commits);
                nextCommits = response.data.newer;
                _prefetchOneMoreCommit();
                eventsEmitter.triggerAsyncCommitsCounterRefresh();
                return commits;
            });
        }

        function commitDetails(sha) {
            return Commits.get({sha: sha}).$then(function(response) {
                return response.data;
            });
        }

        function markAsReviewed(sha) {
            function removeGivenAndAppendPrefetchedCommit(prefetchedCommit) {
                var indexRemoved = commits.removeFromListBy(sha);
                prefetchedCommit && commits.push(prefetchedCommit);
                eventsEmitter.triggerCounterDecrease();
                return commits.elementAtIndexOrLast(indexRemoved);
            }

            var commitMarkedAsReviewed =Commits.remove({sha: sha}).$then(function() {
                return prefetchedCommitPromise;
            });
            var nextCommitToReview = commitMarkedAsReviewed.then(removeGivenAndAppendPrefetchedCommit)
            nextCommitToReview.then(_prefetchOneMoreCommit);
            return nextCommitToReview;
        }

        function loadNextCommits() {
            if(!commits.length) return;
            var options = {};
            options[self.urlParams.min] = commits.last().sha;
            options[self.urlParams.limit] = pageLimit;
            options[self.urlParams.branch] = branchesService.selectedBranch();
            Commits.queryReviewable(options).$then(function(response) {
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
            var options = {};
            options[self.urlParams.min] = commits.last().sha;
            options[self.urlParams.limit] = 1;
            options[self.urlParams.filter] = 'to_review';
            options[self.urlParams.branch] = branchesService.selectedBranch();
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