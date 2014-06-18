angular.module('codebrag.commits')

    .factory('pendingCommitsListService', function(Commits, $rootScope, events, currentRepoContext) {

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
            options[self.urlParams.branch] = currentRepoContext.branch;
            options[self.urlParams.repo] = currentRepoContext.repo;
            return Commits.queryReviewable(options).$then(function(response) {
                var list = _mixInreviewStateMethods(response.data.commits);
                commits.replaceWith(list);
                nextCommits = response.data.newer;
                _prefetchOneMoreCommit();
                return commits;
            });
        }

        function commitDetails(sha) {
            return Commits.get({sha: sha, repo: currentRepoContext.repo}).$then(function(response) {
                return response.data;
            });
        }

        function markAsReviewed(sha) {
            function removeGivenAndAppendPrefetchedCommit(prefetchedCommit) {
                var indexRemoved = commits.removeFromListBy(sha);
                prefetchedCommit && commits.push(_mixInreviewStateMethods(prefetchedCommit));
                eventsEmitter.triggerCommitReviewedEvent();
                return commits.elementAtIndexOrLast(indexRemoved);
            }

            var commitMarkedAsReviewed =Commits.remove({sha: sha, repo: currentRepoContext.repo}).$then(function() {
                return prefetchedCommitPromise;
            });
            var nextCommitToReview = commitMarkedAsReviewed.then(removeGivenAndAppendPrefetchedCommit);
            nextCommitToReview.then(_prefetchOneMoreCommit);
            return nextCommitToReview;
        }

        function loadNextCommits() {
            if(!commits.length) return;
            var options = {};
            options[self.urlParams.min] = commits.last().sha;
            options[self.urlParams.limit] = pageLimit;
            options[self.urlParams.branch] = currentRepoContext.branch;
            options[self.urlParams.repo] = currentRepoContext.repo;
            Commits.queryReviewable(options).$then(function(response) {
                var list = _mixInreviewStateMethods(response.data.commits);
                commits.appendAll(list);
                nextCommits = response.data.newer;
                eventsEmitter.notifyIfNextCommitsLoaded(response.data.commits.length);
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
            options[self.urlParams.branch] = currentRepoContext.branch;
            options[self.urlParams.repo] = currentRepoContext.repo;
            prefetchedCommitPromise = Commits.querySilent(options).$then(function(response) {
                if(!response.data.commits.length) nextCommits = 0;
                return response.data.commits.shift();
            });
        }

        function _mixInreviewStateMethods(commits) {
            if(commits instanceof Array) {
                return commits.map(function(c) {
                    codebrag.commit.mixins.withReviewStateMethods.call(c);
                    return c
                });
            } else {
                codebrag.commit.mixins.withReviewStateMethods.call(commits);
                return commits;
            }
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