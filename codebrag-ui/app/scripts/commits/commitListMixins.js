var codebrag = codebrag || {};
codebrag.commitsList = codebrag.commitsList || {};
codebrag.commitsList.mixin = codebrag.commitsList.mixin || {};

codebrag.commitsList.mixin.withBulkElementsManipulation = function() {

    this.prependAll = function(newCollection) {
        this.unshift.apply(this, newCollection);
        return this;
    };

    this.appendAll = function(newCollection) {
        this.push.apply(this, newCollection);
        return this;
    };

    this.replaceWith = function(newCollection) {
        this.length = 0;
        this.push.apply(this, newCollection);
        return this;
    };

};

codebrag.commitsList.mixin.withMarkingAsReviewed = function() {

    var self = this;

    this.removeFromListBy = function(sha) {
        var toRemove = findIndexOfElementBy(sha);
        var indexToRemoveAt = this.indexOf(toRemove);
        this.splice(indexToRemoveAt, 1);
        return indexToRemoveAt;
    };

    this.markAsReviewedOnly = function(sha) {
        var toMarkAsReviewed = findIndexOfElementBy(sha);
        toMarkAsReviewed && (toMarkAsReviewed.pendingReview = false);
        return this.indexOf(toMarkAsReviewed);
    };

    function findIndexOfElementBy(sha) {
        return _.find(self, function(commit) {
            return commit.sha === sha;
        });
    }

};

codebrag.commitsList.mixin.withIndexOperations = function() {

    this.elementAtIndexOrLast = function(index) {
        var el = this[index];
        if(el) {
            return el;
        }
        return this.last();
    };

    this.elementAtIndex = function(index) {
        if(index < this.length) {
            return this[index];
        }
        return null;
    };

    this.last = function() {
        return this[this.length -1];
    };

    this.first = function() {
        return this[0];
    };

    this.empty = function() {
        return this.length === 0;
    };

};

codebrag.commitsList.mixin.eventsEmitter = function($rootScope, events) {

    return {
        notifyIfNextCommitsLoaded: function(count) {
            count && $rootScope.$broadcast(events.nextCommitsLoaded);
        },
        notifyIfPreviousCommitsLoaded: function(count) {
            count && $rootScope.$broadcast(events.previousCommitsLoaded);
        },
        triggerCounterDecrease: function() {
            $rootScope.$broadcast(events.commitReviewed);
        },
        triggerAsyncCommitsCounterRefresh: function() {
            $rootScope.$broadcast(events.refreshCommitsCounter);
        }
    }

}