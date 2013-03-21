'use strict';

describe("Commits Controller", function () {

    beforeEach(module('codebrag.commits'));

    afterEach(inject(function(_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var scope, $httpBackend, ctrl, pendingCommits;

    beforeEach(inject(function (_$httpBackend_, $rootScope, $controller, PendingCommits) {
        $httpBackend = _$httpBackend_;
        pendingCommits = PendingCommits;
        scope = $rootScope.$new();
        ctrl = $controller
    }));

    it('should fetch pending commits from server', function() {
        // Given
        var response = {commits: [{message: 'sample msg', sha: '123abc'}]};
        $httpBackend.whenGET('rest/commits?type=pending').respond(response);

        // When
        ctrl('CommitsCtrl', {$scope: scope});
        $httpBackend.flush();

        //Then
        expect(scope.commits).toEqual(response.commits);
    });

});
