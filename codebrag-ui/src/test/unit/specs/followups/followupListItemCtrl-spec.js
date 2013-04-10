'use strict';

var Controller

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
    var followup3 = {
        userId: "1243",
        date: "2013-01-04T06:39:12Z",
        commit: {
            commitId: "3",
            message: "Add more tests for deleting follow-ups on frontend",
            authorName: "KC",
            date: "2012-04-02T05:39:12Z"
        }
    }

    var emptyTestState = {}; //empty state to avoid state initialization, fetching templates, resolving etc

    beforeEach(module('codebrag.followups'));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));


    beforeEach(inject(function (_$httpBackend_) {
        $httpBackend = _$httpBackend_;
    }));

    it('should remove first element from model after receiving confirmation from the backend', inject(function ($controller) {

        $httpBackend.whenDELETE('rest/followups/1').respond();

        var scope = {followups: [followup1, followup2]};

        // When
        $controller('FollowupListItemCtrl', {$scope: scope, $state: emptyTestState});
        scope.dismiss(followup1)
        $httpBackend.flush();

        //Then
        expect(scope.followups).toEqual([followup2]);
    }));

    it('should remove last element from model after receiving confirmation from the backend', inject(function ($controller) {

        $httpBackend.whenDELETE('rest/followups/3').respond();

        var scope = {followups: [followup1, followup2, followup3]};

        // When
        $controller('FollowupListItemCtrl', {$scope: scope, $state: emptyTestState});
        scope.dismiss(followup3)
        $httpBackend.flush();

        //Then
        expect(scope.followups).toEqual([followup1, followup2]);
    }));

    it('should remove single element from model after receiving confirmation from the backend', inject(function ($controller) {

        $httpBackend.whenDELETE('rest/followups/1').respond();

        var scope = {followups: [followup1]};

        // When
        $controller('FollowupListItemCtrl', {$scope: scope, $state: emptyTestState});
        scope.dismiss(followup1)
        $httpBackend.flush();

        //Then
        expect(scope.followups).toEqual([]);
    }));

    it('should remove middle element from model after receiving confirmation from the backend', inject(function ($controller) {

        $httpBackend.whenDELETE('rest/followups/2').respond();

        var scope = {followups: [followup1, followup2, followup3]};

        // When
        $controller('FollowupListItemCtrl', {$scope: scope, $state: emptyTestState});
        scope.dismiss(followup2)
        $httpBackend.flush();

        //Then
        expect(scope.followups).toEqual([followup1, followup3]);
    }));
});
