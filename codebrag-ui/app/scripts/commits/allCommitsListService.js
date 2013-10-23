angular.module('codebrag.commits')

    .factory('allCommitsListService', function(Commits, $rootScope, events, $q) {

        var pageLimit = 7;

        var nextCommits, previousCommits;

        var commits = [];
        codebrag.commitsList.mixin.withBulkElementsManipulation.call(commits);
        codebrag.commitsList.mixin.withMarkingAsReviewed.call(commits);
        codebrag.commitsList.mixin.withIndexOperations.call(commits);

        var eventsEmitter = codebrag.commitsList.mixin.eventsEmitter($rootScope, events);

        function loadCommits(commitId) {
            var options = {limit: pageLimit};
            commitId && angular.extend(options, {id: commitId});
            return Commits.queryWithSurroundings(options).$then(function(response) {
                commits.replaceWith(response.data.commits);
                previousCommits = response.data.older;
                nextCommits = response.data.newer;
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
            Commits.remove({commitId: commitId});   // fire and don't wait for response
            var indexReviewed = commits.markAsReviewedOnly(commitId);
            eventsEmitter.triggerCounterDecrease();
            return $q.when(commits.elementAtIndex(indexReviewed + 1));
        }

        function loadNextCommits() {
            if(!commits.length) return;
            var options = {min_id: commits.last().id, limit: pageLimit, filter: 'all'};
            return Commits.query(options).$then(function(response) {
                commits.appendAll(response.data.commits);
                nextCommits = response.data.newer;
                eventsEmitter.notifyIfNextCommitsLoaded(response.data.commits.length);
                eventsEmitter.triggerAsyncCommitsCounterRefresh();
                return commits;
            });
        }

        function loadPreviousCommits() {
            if(!commits.length) return;
            var options = {max_id: commits.first().id, limit: pageLimit, filter: 'all'};
            return Commits.query(options).$then(function(response) {
                commits.prependAll(response.data.commits);
                previousCommits = response.data.older;
                eventsEmitter.notifyIfPreviousCommitsLoaded(response.data.commits.length);
                eventsEmitter.triggerAsyncCommitsCounterRefresh();
                return commits;
            });
        }

        function hasNextCommits() {
            return !!nextCommits;
        }

        function hasPreviousCommits() {
            return !!previousCommits;
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