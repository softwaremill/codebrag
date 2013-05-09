var codebrag = codebrag || {};

/**
 *
 * Collection of object backed by async operations
 * Maintains immutable reference to collection of elements
 * Loading and removing elements is based on promises.
 *
 * Used to commits and followups to keep server state in sync with local view
 *
 */
codebrag.AsyncCollection = function() {

    // initialize with empty elements list
    this.elements = [];

};

codebrag.AsyncCollection.prototype = {

    loadElements: function (promise) {
        var self = this;
        return promise.then(function(receivedCollection) {
            self._replaceElementsWith(receivedCollection);
            return self.elements;
        })
    },

    _replaceElementsWith: function(source) {
        var self = this;
        self.elements.length = 0;
        _.forEach(source, function(el) {
            self.elements.push(el);
        })
    },

    removeElement: function(matchFn, promise) {
        var self = this;
        var indexToRemove = self._indexOf(matchFn);
        return promise.then(function() {
            self.elements.splice(indexToRemove, 1);
            return indexToRemove;
        });
    },

    removeElementAndGetNext: function(matchFn, promise) {
        var self = this;
        return self.removeElement(matchFn, promise).then(self._getNext.bind(self));
    },

    _indexOf: function(matchFn) {
        var self = this;
        var found = _.find(self.elements, function(current) {
            return matchFn(current);
        });
        return self.elements.indexOf(found);
    },

    _getNext: function (index) {
        var self = this;
        if (_.isEmpty(self.elements)) {
            return null;
        }
        if (index === 0) {
            return self.elements[0];
        }
        return self.elements[index - 1];
    }
};