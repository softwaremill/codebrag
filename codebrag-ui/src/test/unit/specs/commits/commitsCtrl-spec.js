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

    it('should fetch commits when controller starts', inject(function($controller) {
        // Given
        var scope = {};
        $httpBackend.expectGET('rest/commits').respond();

        // When
        $controller('CommitsCtrl', {$scope: scope});

        //Then
        $httpBackend.flush();
    }));

    it('should expose loading commits as a scope function', inject(function($controller, commitsListService) {
        // Given
        var scope = {};
        $httpBackend.whenGET('rest/commits').respond(commitsList);
        spyOn(commitsListService, 'allCommits');
        $controller('CommitsCtrl', {$scope: scope});

        // When
        scope.commits();
        $httpBackend.flush();

        //Then
        expect(commitsListService.allCommits).toHaveBeenCalled();
    }));

});
