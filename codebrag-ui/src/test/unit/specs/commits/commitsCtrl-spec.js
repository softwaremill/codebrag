'use strict';

describe("Commits Controller", function () {

    var commitsList = [{message: 'sample msg', sha: '123abc'}];
    var $httpBackend;

    beforeEach(module('codebrag.commits'));

    afterEach(inject(function(_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));


    beforeEach(inject(function (_$httpBackend_) {
        $httpBackend = _$httpBackend_;
    }));

    it('should fetch commits according to selected load mode', inject(function($controller, commitsListService) {
        // Given
        var scope = {};
        var commitsReturnedByService = [];
        spyOn(commitsListService, 'loadCommitsFromServer');
        spyOn(commitsListService, 'allCommits').andReturn(commitsReturnedByService);

        // When
        $controller('CommitsCtrl', {$scope: scope});
        expect(commitsListService.loadCommitsFromServer).toHaveBeenCalledWith(LOAD_MODE.ONLY_PENDING);
        scope.loadMode = {
            value: 'all'
        };
        scope.loadCommits();

        //Then
        expect(commitsListService.loadCommitsFromServer).toHaveBeenCalledWith(LOAD_MODE.WITH_REVIEWED);
        expect(scope.commits).toBe(commitsReturnedByService)
    }));

    it('should fetch commits pending review when controller starts', inject(function($controller, commitsListService) {
        // Given
        var scope = {};
        var commitsReturnedByService = [];
        spyOn(commitsListService, 'loadCommitsFromServer');
        spyOn(commitsListService, 'allCommits').andReturn(commitsReturnedByService);

        // When
        $controller('CommitsCtrl', {$scope: scope});

        //Then
        expect(commitsListService.loadCommitsFromServer).toHaveBeenCalledWith(LOAD_MODE.ONLY_PENDING);
        expect(scope.commits).toBe(commitsReturnedByService)
    }));

    it('should expose loading commits via scope', inject(function($controller, commitsListService) {
        // Given
        var scope = {};
        spyOn(commitsListService, 'loadCommitsFromServer');
        spyOn(commitsListService, 'allCommits').andReturn(commitsList);

        // When
        $controller('CommitsCtrl', {$scope: scope});

        //Then
        var commit = scope.commits[0];
        expect(commit.sha).toBe(commitsList[0].sha);
    }));

    var LOAD_MODE = {
        WITH_REVIEWED: 'all',
        ONLY_PENDING: 'pending'
    }
});
