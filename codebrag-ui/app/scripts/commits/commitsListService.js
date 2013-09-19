angular.module('codebrag.commits')

    .service('commitsListService', function(Commits, $rootScope, events, $q) {

        var self = this;

        var pageLimit = 7;

        var totalCommitsToReviewCount = 0;

        var commitsListLoadFilter = {
            values: {all: 'all', toReview: 'to_review'},
            current: null,
            setAll: function() {this.current = this.values.all;},
            setToReview: function() {this.current = this.values.toReview;},
            isAll: function() {return this.current === this.values.all;},
            isToReview: function() {return this.current === this.values.toReview;}
        };

        var commits = [];
        codebrag.commitsList.mixin.withBulkElementsManipulation.call(commits);
        codebrag.commitsList.mixin.withMarkingAsReviewed.call(commits);
        codebrag.commitsList.mixin.withIndexOperations.call(commits);


        var nextCommitsAvailable = false;
        var previousCommitsAvailable = false;

        this.hasNextCommits = function() {
            return nextCommitsAvailable;
        };

        this.hasPreviousCommits = function() {
            return previousCommitsAvailable;
        };

        this.loadCommitsToReview = function() {
            commitsListLoadFilter.setToReview();
            return Commits.queryReviewable({limit: pageLimit}).$then(function(response) {
                commits.replaceWith(response.data.commits);
                updatePreviousCommitsAvailability(response.data.older);
                updateNextCommitsAvailability(response.data.newer);
                updatePendingCommitsCounter(response.data.totalCount);
                return commits;
            });
        };

        this.loadCommitsInContext = function(commitId) {
            commitsListLoadFilter.setAll();
            var options = {id: commitId, limit: pageLimit};
            return Commits.queryWithSurroundings(options).$then(function(response) {
                commits.replaceWith(response.data.commits);
                var loadedCommitsCount = howManyNextCommitsLoadedOnBothSides(response.data.commits);
                updatePreviousCommitsAvailability(loadedCommitsCount.prevCommitsLoaded);
                updateNextCommitsAvailability(loadedCommitsCount.nextCommitsLoaded);
                updatePendingCommitsCounter(response.data.totalCount);
                return commits;
            });

            function howManyNextCommitsLoadedOnBothSides(loadedCommits) {
                var centralCommit = _.find(loadedCommits, function(commit) {
                    return commit.id === commitId;
                });
                var indexOfCentral = loadedCommits.indexOf(centralCommit);
                var result = {
                    prevCommitsLoaded: loadedCommits.length,
                    nextCommitsLoaded: loadedCommits.length
                };
                if (indexOfCentral !== -1) {
                    result.prevCommitsLoaded = loadedCommits.slice(0, indexOfCentral).length;
                    result.nextCommitsLoaded = loadedCommits.slice(indexOfCentral + 1).length;
                }
                return result;
            }
        };

        this.loadNewestCommits = function() {
            commitsListLoadFilter.setAll();
            var options = {limit: pageLimit};
            return Commits.queryWithSurroundings(options).$then(function(response) {
                commits.replaceWith(response.data.commits);
                updatePreviousCommitsAvailability(response.data.older);
                updateNextCommitsAvailability(response.data.newer);
                updatePendingCommitsCounter(response.data.totalCount);
                return commits;
            });
        };

        this.loadCommitDetails = function(commitId) {
            var options = {
                commitId: commitId
            };
            return Commits.get(options).$then(function(response) {
                return response.data;
            });
        };

        this.makeReviewedAndGetNext = function(commitId) {
            if(commitsListLoadFilter.isToReview()) {
                return makeReviewedAndGetNextInToReviewMode(commitId);
            } else {
                return makeReviewedAndGetNextInAllMode(commitId);
            }
        };

        this.loadNextCommits = function(limit) {
            if(!this.hasNextCommits()) {
                return $q.when();
            }
            var options = {min_id: commits.last().id, limit: limit || pageLimit};
            options = angular.extend(options, {filter: commitsListLoadFilter.current});
            return Commits.query(options).$then(function(response) {
                commits.appendAll(response.data.commits);
                updatePendingCommitsCounter(response.data.totalCount);
                updateNextCommitsAvailability(response.data.newer);
                notifyIfNextCommitsLoaded(response.data.commits.length);
            });
        };

        this.loadPreviousCommits = function(limit) {
            if(!this.hasPreviousCommits()) {
                return $q.when();
            }
            var options = {max_id: commits.first().id, limit: limit || pageLimit};
            options = angular.extend(options, {filter: commitsListLoadFilter.current});
            return Commits.query(options).$then(function(response) {
                commits.prependAll(response.data.commits);
                updatePendingCommitsCounter(response.data.totalCount);
                updatePreviousCommitsAvailability(response.data.older);
                notifyIfPreviousCommitsLoaded(response.data.commits.length);
            });
        };

        function makeReviewedAndGetNextInToReviewMode(commitId) {
            var removedAtIndex;
            return Commits.remove({commitId: commitId}).$then(function() {
                removedAtIndex = commits.removeFromListBy(commitId);
                updatePendingCommitsCounter(totalCommitsToReviewCount - 1);
                return self.loadNextCommits(1);
            }).then(function() {
                return commits.elementAtIndexOrLast(removedAtIndex);
            });
        }

        function makeReviewedAndGetNextInAllMode(commitId) {
            return Commits.remove({commitId: commitId}).$then(function() {
                var atIndex = commits.markAsReviewedOnly(commitId);
                updatePendingCommitsCounter(totalCommitsToReviewCount - 1);
                return $q.when(commits.elementAtIndexOrLast(atIndex + 1));
            });
        }

        function notifyIfNextCommitsLoaded(count) {
            count && $rootScope.$broadcast(events.nextCommitsLoaded);
        }

        function notifyIfPreviousCommitsLoaded(count) {
            count && $rootScope.$broadcast(events.previousCommitsLoaded);
        }

        function updatePendingCommitsCounter(newCount) {
            totalCommitsToReviewCount = newCount;
            $rootScope.$broadcast(events.commitCountChanged, {commitCount: newCount});
        }

        function updateNextCommitsAvailability(newerCommits) {
            nextCommitsAvailable = newerCommits > 0;
        }

        function updatePreviousCommitsAvailability(olderCommits) {
            previousCommitsAvailable = olderCommits > 0;
        }

    });