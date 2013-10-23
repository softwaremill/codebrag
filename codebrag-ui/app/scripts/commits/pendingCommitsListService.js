angular.module('codebrag.commits')

    .factory('pendingCommitsListService', function(Commits, $rootScope, events, $q) {

        var pageLimit = 7;

        var nextCommits;
        var prefetchedCommit;

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
            console.log(commits.length);
            var indexRemoved = commits.removeFromListBy(commitId);
            console.log(commits.length);
            prefetchedCommit && commits.push(prefetchedCommit);
            console.log(commits.length);
            eventsEmitter.triggerCounterDecrease();
            Commits.remove({commitId: commitId});
            _prefetchOneMoreCommit();
            return commits.elementAtIndexOrLast(indexRemoved);
        }

        function loadNextCommits() {
            if(!commits.length) return;
            var options = {min_id: commits.last().id, limit: pageLimit, filter: 'to_review'};
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
            var options = {min_id: commits.last().id, limit: 1, filter: 'to_review'};
            console.log('pref1');
            Commits.querySilent(options).$then(function(response) {
                console.log('pref2');
                console.log(response.data);
                prefetchedCommit = response.data.commits.shift();
                if(!prefetchedCommit) nextCommits = 0;
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