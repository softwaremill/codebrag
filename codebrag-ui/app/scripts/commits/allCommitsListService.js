angular.module('codebrag.commits')

    .factory('allCommitsListService', function(Commits, $rootScope, events, $q, currentRepoContext) {

        var self = this;

        var pageLimit = 7;

        var nextCommits, previousCommits;

        var commits = [];
        codebrag.commitsList.mixin.withBulkElementsManipulation.call(commits);
        codebrag.commitsList.mixin.withMarkingAsReviewed.call(commits);
        codebrag.commitsList.mixin.withIndexOperations.call(commits);

        codebrag.commitsList.mixin.urlParams(this);

        var eventsEmitter = codebrag.commitsList.mixin.eventsEmitter($rootScope, events);

        function loadCommits(sha) {
            var options = {};
            options[self.urlParams.limit] = pageLimit;
            options[self.urlParams.selected] = sha;
            options[self.urlParams.branch] = currentRepoContext.branch;
            options[self.urlParams.repo] = currentRepoContext.repo;
            return Commits.queryAllWithSurroundings(options).$then(function(response) {
                var list = _mixInreviewStateMethods(response.data.commits);
                commits.replaceWith(list);
                previousCommits = response.data.older;
                nextCommits = response.data.newer;
                return commits;
            });
        }

        function commitDetails(sha) {
            return Commits.get({sha: sha}).$then(function(response) {
                return response.data;
            });
        }

        function markAsReviewed(sha) {
            Commits.remove({sha: sha});   // fire and don't wait for response
            var indexReviewed = commits.markAsReviewedOnly(sha);
            eventsEmitter.triggerCommitReviewedEvent();
            return $q.when(commits.elementAtIndex(indexReviewed + 1));
        }

        function loadNextCommits() {
            if(!commits.length) return;
            var options = {};
            options[self.urlParams.min] = commits.last().sha;
            options[self.urlParams.limit] = pageLimit;
            options[self.urlParams.branch] = currentRepoContext.branch;
            options[self.urlParams.repo] = currentRepoContext.repo;
            return Commits.queryAll(options).$then(function(response) {
                var list = _mixInreviewStateMethods(response.data.commits);
                commits.appendAll(list);
                nextCommits = response.data.newer;
                eventsEmitter.notifyIfNextCommitsLoaded(response.data.commits.length);
                return commits;
            });
        }

        function loadPreviousCommits() {
            if(!commits.length) return;
            var options = {};
            options[self.urlParams.max] = commits.first().sha;
            options[self.urlParams.limit] = pageLimit;
            options[self.urlParams.branch] = currentRepoContext.branch;
            options[self.urlParams.repo] = currentRepoContext.repo;
            return Commits.queryAll(options).$then(function(response) {
                var list = _mixInreviewStateMethods(response.data.commits);
                commits.prependAll(list);
                previousCommits = response.data.older;
                eventsEmitter.notifyIfPreviousCommitsLoaded(response.data.commits.length);
                return commits;
            });
        }

        function hasNextCommits() {
            return !!nextCommits;
        }

        function hasPreviousCommits() {
            return !!previousCommits;
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
            loadPreviousCommits: loadPreviousCommits,
            markAsReviewed: markAsReviewed,
            hasNextCommits: hasNextCommits,
            hasPreviousCommits: hasPreviousCommits
        }

    });