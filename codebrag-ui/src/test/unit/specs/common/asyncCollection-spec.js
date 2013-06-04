describe("Async Collection", function () {

    var rootScope;

    beforeEach(module('codebrag.common'));

    beforeEach(inject(function ($rootScope) {
        rootScope = $rootScope;
    }));

    it('should return null as next for empty collection', inject(function ($q, $rootScope) {
        // Given
        var collection = new codebrag.AsyncCollection();
        var nextElement = undefined;
        var elementIdToSearch = 1;
        var deferred = $q.defer();
        deferred.resolve();

        // When
        collection.getNextAfter(_matchingId(elementIdToSearch), deferred.promise).then(function(found) {
            nextElement = found;
        });
        $rootScope.$apply();

        // Then
        expect(nextElement).toBeNull();
    }));

    it('should return first element as next for collection with one element', inject(function ($q, $rootScope) {
        // Given
        var collection = new codebrag.AsyncCollection();
        var nextElement = undefined;
        var addedElement = {id: 1};
        var elementIdToSearch = 1;
        var addPromise = _givenCollectionHasElements(collection, [addedElement], $q);

        // When
        collection.getNextAfter(_matchingId(elementIdToSearch), addPromise).then(function(found) {
            nextElement = found;
        });
        $rootScope.$apply();

        // Then
        expect(nextElement).toEqual(addedElement);
    }));


    it('should return second element element as next for collection with two elements', inject(function ($q, $rootScope) {
        // Given
        var collection = new codebrag.AsyncCollection();
        var nextElement = undefined;
        var addedElements = [{id: 1}, {id: 2}];
        var elementIdToSearch = 1;
        var addPromise = _givenCollectionHasElements(collection, addedElements, $q);

        // When
        collection.getNextAfter(_matchingId(elementIdToSearch), addPromise).then(function(found) {
            nextElement = found;
        });
        $rootScope.$apply();

        // Then
        expect(nextElement).toEqual(addedElements[1]);
    }));

    it('should return last element as next for element matching at last position', inject(function ($q, $rootScope) {
        // Given
        var collection = new codebrag.AsyncCollection();
        var nextElement = undefined;
        var addedElements = [{id: 1}, {id: 2}];
        var elementIdToSearch = 2;
        var addPromise = _givenCollectionHasElements(collection, addedElements, $q);

        // When
        collection.getNextAfter(_matchingId(elementIdToSearch), addPromise).then(function(found) {
            nextElement = found;
        });
        $rootScope.$apply();

        // Then
        expect(nextElement).toEqual(addedElements[1]);
    }));

    it('should return null as next when removing last element', inject(function ($q, $rootScope) {
        // Given
        var collection = new codebrag.AsyncCollection();
        var nextElement = undefined;
        var addedElements = [{id: 1}];
        var elementIdToRemove = 1;
        var addPromise = _givenCollectionHasElements(collection, addedElements, $q);

        // When
        collection.removeElementAndGetNext(_matchingId(elementIdToRemove), addPromise).then(function(found) {
            nextElement = found;
        });
        $rootScope.$apply();

        // Then
        expect(nextElement).toBeNull();
    }));

    it('should return element on same position when removing and getting next', inject(function ($q, $rootScope) {
        // Given
        var collection = new codebrag.AsyncCollection();
        var nextElement = undefined;
        var addedElements = [{id: 1}, {id: 2}, {id: 3}, {id: 4}];
        var elementIdToRemove = 2;
        var addPromise = _givenCollectionHasElements(collection, addedElements, $q);

        // When
        collection.removeElementAndGetNext(_matchingId(elementIdToRemove), addPromise).then(function(found) {
            nextElement = found;
        });
        $rootScope.$apply();

        // Then
        expect(collection.elements.length).toBe(3);
        expect(nextElement).toEqual({id: 3});
    }));

    function _givenCollectionHasElements(collection, elements, $q) {
        var deferred = $q.defer();
        deferred.resolve(elements);
        return collection.addElements(deferred.promise);
    }

    function _matchingId(id) {
        return function(element) {
            return element.id == id;
        }
    }


});
