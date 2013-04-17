'use strict';

describe("Commits Controller", function () {

    var commitsList = {commits: [{message: 'sample msg', sha: '123abc'}]};
    var $httpBackend;

    beforeEach(module('codebrag.commits'));

    afterEach(inject(function(_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));


    beforeEach(inject(function (_$httpBackend_) {
        $httpBackend = _$httpBackend_;
    }));

    it('should fetch pending commits from server', inject(function($controller) {
        // Given
        var scope = {};
        $httpBackend.whenGET('rest/commits').respond(commitsList);

        // When
        $controller('CommitsCtrl', {$scope: scope});
        $httpBackend.flush();

        //Then
        expect(scope.commits).toEqual(commitsList.commits);
    }));

});
