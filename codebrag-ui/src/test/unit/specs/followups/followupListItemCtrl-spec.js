'use strict';

describe("Follow-ups list item Controller", function () {

    var $httpBackend;
    var followup1 = {
        userId: "123",
        date: "2013-04-03T06:39:12Z",
        commit: {
            commitId: "1",
            message: "Perform redirect to GitHub from server\n\nAdd client id on server and build url to redirect to, then send 302 with new location.",
            authorName: "Piotr Buda",
            date: "2013-04-02T06:39:12Z"
        }
    }
    var followup2 = {
        userId: "123",
        date: "2013-04-04T06:39:12Z",
        commit: {
            commitId: "2",
            message: "Some minor refactoring",
            authorName: "John Doe",
            date: "2013-04-02T05:39:12Z"
        }
    }

    beforeEach(module('codebrag.followups'));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));


    beforeEach(inject(function (_$httpBackend_) {
        $httpBackend = _$httpBackend_;
    }));

    it('should remove element from model after receiving confirmation from the backend', inject(function ($controller) {

        $httpBackend.whenDELETE('rest/followups/1').respond();

        var scope = {followups: [followup1, followup2]};

        // When
        $controller('FollowupListItemCtrl', {$scope: scope});
        scope.dismiss(followup1)
        $httpBackend.flush();

        //Then
        expect(scope.followups).toEqual([followup2]);
    }));

});
