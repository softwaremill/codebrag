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

    addElements: function(promise) {
        var self = this;
        return promise.then(function(receivedCollection) {
            self.elements = self.elements.concat(receivedCollection);
            return self.elements;
        });
    },

    /**
     * Fills collection with elements returned by a promise.
     * @param promise a promise which should return an array of elements.
     * @returns a promise of array of elements loaded into this collection.
     */
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

    /**
     * Removes element matching gives criteria in matchFn. Removal operation will be performed
     * asynchronously and chained to given promise (for example a server response promise).
     * @param matchFn matching function taking single element and returning true or false if it matches criteria.
     * @param promise of call which should be succeeded by removing element.
     * @returns a promise of removing matching element and returning index of that element.
     */
    removeElement: function(matchFn, promise) {
        var self = this;
        var indexToRemove = self._indexOf(matchFn);
        return promise.then(function() {
            self.elements.splice(indexToRemove, 1);
            return indexToRemove;
        });
    },

    /**
     * Removes element matching gives criteria in matchFn. Removal operation will be performed
     * asynchronously and chained to given promise (for example a server response promise).
     * @param matchFn matching function taking single element and returning true or false if it matches criteria.
     * @param promise of call which should be succeeded by removing element.
     * @returns a promise of removing matching element and returning next element in the collection (previous
     * element in the array) or null if removed first element.
     */
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

    /**
     * Returns a promise of element 'next' after element matching given criteria, technically the previous element in array.
     * @param matchFn function to match element whose successor should be returned.
     * @param promise a promise which, when fulfilled, should be followed by returning the element.
     * @returns a promise of element matching criteria.
     */
    getNextAfter: function (matchFn, promise) {
        var self = this;
        return promise.then(function() {
            return self._getNext(self._indexOf(matchFn));
        });
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