'use strict';

describe("Follow-ups Controller", function () {

    var followupList = {followups: [
        {
            userId: "123",
            date: "2013-04-03T06:39:12Z",
            commit: {
                commitId: "515bd590e4b06701221f9e37",
                message: "Perform redirect to GitHub from server\n\nAdd client id on server and build url to redirect to, then send 302 with new location.",
                authorName: "Piotr Buda",
                date: "2013-04-02T06:39:12Z"
            }
        }
    ]
    };

    var $httpBackend;

    beforeEach(module('codebrag.followups'));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));


    beforeEach(inject(function (_$httpBackend_) {
        $httpBackend = _$httpBackend_;
    }));

    it('should fetch follow-ups from server', inject(function ($controller) {
        // Given
        var scope = {};
        $httpBackend.whenGET('rest/followups/').respond(followupList);

        // When
        $controller('FollowupsCtrl', {$scope: scope});
        $httpBackend.flush();

        //Then
        expect(scope.followups).toEqual(followupList.followups);
    }));

});
