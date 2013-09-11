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

    this.removeFromListBy = function(commitId) {
        var toRemove = findIndexOfElementBy(commitId);
        var indexToRemoveAt = this.indexOf(toRemove);
        this.splice(indexToRemoveAt, 1);
        return indexToRemoveAt;
    };

    this.markAsReviewedOnly = function(commitId) {
        var toMarkAsReviewed = findIndexOfElementBy(commitId);
        toMarkAsReviewed && (toMarkAsReviewed.pendingReview = false);
        return this.indexOf(toMarkAsReviewed);
    };

    function findIndexOfElementBy(commitId) {
        return _.find(self, function(commit) {
            return commit.id === commitId;
        });
    }

};

codebrag.commitsList.mixin.withIndexOperations = function() {

    this.elementAtIndexOrLast = function(index) {
        var el = this[index];
        if(el) {
            return el;
        } else {
            return this.last();
        }
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