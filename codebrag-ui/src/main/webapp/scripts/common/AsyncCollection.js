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
        promise.then(function(receivedCollection) {
            self._replaceElementsWith(receivedCollection);
        })
    },

    _replaceElementsWith: function(source) {
        var self = this;
        self.elements.length = 0;
        _.forEach(source, function(el) {
            self.elements.push(el);
        })
    },

    removeElement: function(identityFn, promise) {
        var self = this;
        var found = _.find(self.elements, function(current) {
            return identityFn(current);
        });
        var indexToRemove = self.elements.indexOf(found);
        return promise.then(function() {
            self.elements.splice(indexToRemove, 1);
            return indexToRemove;
        });
    },

    removeElementAndGetNext: function(identityFn, promise) {
        var self = this;
        return self.removeElement(identityFn, promise).then(function(indexRemoved) {
            if(_.isEmpty(self.elements)) {
                return null;
            }
            if(indexRemoved === 0) {
                return self.elements[0];
            }
            return self.elements[indexRemoved - 1];
        });
    }
};
