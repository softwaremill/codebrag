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

    it('should expose loading commits via scope', inject(function($controller, commitsListService) {
        // Given
        var scope = {};
        $httpBackend.whenGET('rest/commits').respond(commitsList);
        $controller('CommitsCtrl', {$scope: scope});

        // When
        $httpBackend.flush();

        //Then
        var commit = scope.commits[0];
        expect(commit.sha).toBe(commitsList.commits[0].sha);
    }));

});
