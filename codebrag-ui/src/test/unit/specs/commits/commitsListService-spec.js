'use strict';

describe("CommitDetailsController", function () {

    var $httpBackend;
    var commitsListUrl = 'rest/commits';
    var rootScope;

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function (_$httpBackend_, $rootScope) {
        $httpBackend = _$httpBackend_;
        rootScope = $rootScope;
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('remove commit locally and from server', inject(function (commitsListService) {
        // Given
        var loadedCommits = [commit(111), commit(222), commit(333)];
        var commitIdToRemove = 222;
        $httpBackend.whenGET(commitsListUrl).respond({commits:loadedCommits});
        $httpBackend.expectDELETE(commitUrl(commitIdToRemove)).respond(200);
        commitsListService.loadCommitsFromServer();

        // When
        commitsListService.removeCommit(commitIdToRemove);
        $httpBackend.flush();

        // Then
        expect(commitsListService.allCommits().length).toBe(loadedCommits.length - 1);
    }));

    xit('remove commit and return next from list', inject(function (commitsListService, $rootScope) {
        // TODO: test chained promises
        throw 'To be implemented';
    }));

    function commitUrl(id) {
        return 'rest/commits/' + id;
    }

    function commit(id) {
        var idStr = id.toString();
        return {id: idStr, sha: 'sha' + idStr, msg: 'message' + idStr}
    }

});
