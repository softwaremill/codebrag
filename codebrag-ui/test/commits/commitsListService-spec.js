'use strict';

describe("CommitsListService", function () {

    var $httpBackend,
        $rootScope,
        events,
        commitsListService;

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function (_$httpBackend_, _$rootScope_, _commitsListService_, _events_) {
        $httpBackend = _$httpBackend_;
        $rootScope = _$rootScope_;
        events = _events_;
        commitsListService = _commitsListService_;

        this.addMatchers({
            toEqualProps: function(expected) {
                return angular.equals(this.actual, expected);
            }
        });
    }));

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('should load pending commits and update next/prev status', function() {
        // given
        var commits;
        var pendingCommitsResponse = buildCommitsResponse([1, 2, 3, 4, 5, 6, 7], 10);
        $httpBackend.expectGET('rest/commits?filter=to_review&limit=7').respond(pendingCommitsResponse);

        // when
        commitsListService.loadCommitsToReview().then(function(list) {
            commits = list;
        });
        $httpBackend.flush();

        // then
        expect(commits).toEqualProps(pendingCommitsResponse.commits);
        expect(commitsListService.hasNextCommits()).toBeTruthy();
        expect(commitsListService.hasPreviousCommits()).toBeFalsy();
    });

    it('should load commits in context and update next/prev status', function() {
        // given
        var commits;
        var pivotCommitId = 2;
        var contextCommitsResponse = buildCommitsResponse([1, 2, 3, 4, 5, 6, 7, 8, 9], 20);
        $httpBackend.expectGET('rest/commits?context=true&id=2&limit=7').respond(contextCommitsResponse);

        // when
        commitsListService.loadCommitsInContext(pivotCommitId).then(function(list) {
            commits = list;
        });
        $httpBackend.flush();

        // then
        expect(commits).toEqualProps(contextCommitsResponse.commits);
        expect(commitsListService.hasNextCommits()).toBeTruthy();
        expect(commitsListService.hasPreviousCommits()).toBeFalsy();
    });

    it('should replace commits list when loading pending and then all', function() {
        // given
        var pendingCommitsResponse = buildCommitsResponse([1, 2, 3, 4, 5, 6, 7], 10);
        $httpBackend.expectGET('rest/commits?filter=to_review&limit=7').respond(pendingCommitsResponse);

        var contextCommitsResponse = buildCommitsResponse([1, 2, 3, 4, 5, 6, 7, 8, 9], 20);
        $httpBackend.expectGET('rest/commits?context=true&id=2&limit=7').respond(contextCommitsResponse);

        var pivotCommitId = 2;

        // when
        var commits;
        commitsListService.loadCommitsToReview();
        commitsListService.loadCommitsInContext(pivotCommitId).then(function(list) {
            commits = list;
        });
        $httpBackend.flush();

        // then
        expect(commits).toEqualProps(contextCommitsResponse.commits);
    });

    it('should load next commits in all mode', function() {
        // given
        var pivotCommitId = 1;
        var firstCommitsResponse = buildCommitsResponse([1, 2, 3, 4, 5, 6, 7, 8], 30);
        var nextCommitsResponse = buildCommitsResponse([9, 10, 11, 12, 13, 14, 15], 30);
        $httpBackend.expectGET('rest/commits?context=true&id=1&limit=7').respond(firstCommitsResponse);
        $httpBackend.expectGET('rest/commits?filter=all&limit=7&min_id=8').respond(nextCommitsResponse);

        // when
        var commitsList;
        commitsListService.loadCommitsInContext(pivotCommitId).then(function(commits) {
            commitsList = commits;
            commitsListService.loadNextCommits();
        });
        $httpBackend.flush();

        // then
        var allLoadedCommitsCount = firstCommitsResponse.commits.length + nextCommitsResponse.commits.length;
        expect(commitsList.length).toBe(allLoadedCommitsCount);
    });

    it('should load next commits in to_review mode', function() {
        // given
        var firstCommitsResponse = buildCommitsResponse([1, 2, 3, 4, 5, 6, 7], 30);
        var nextCommitsResponse = buildCommitsResponse([9, 10, 11, 12, 13, 14, 15], 30);
        $httpBackend.expectGET('rest/commits?filter=to_review&limit=7').respond(firstCommitsResponse);
        $httpBackend.expectGET('rest/commits?filter=to_review&limit=7&min_id=7').respond(nextCommitsResponse);

        // when
        var commitsList;
        commitsListService.loadCommitsToReview().then(function(commits) {
            commitsList = commits;
            commitsListService.loadNextCommits();
        });
        $httpBackend.flush();

        // then
        var allLoadedCommitsCount = firstCommitsResponse.commits.length + nextCommitsResponse.commits.length;
        expect(commitsList.length).toBe(allLoadedCommitsCount);
    });

    it('should load previous commits in all mode', function() {
        // given
        var firstCommitsResponse = buildCommitsResponse([1, 2, 3, 4, 5, 6, 7, 8], 30);
        var previousCommitsResponse = buildCommitsResponse([-1, -2 , -3], 30);
        $httpBackend.expectGET('rest/commits?context=true&id=8&limit=7').respond(firstCommitsResponse);
        $httpBackend.expectGET('rest/commits?filter=all&limit=7&max_id=1').respond(previousCommitsResponse);

        // when
        var commitsList;
        var pivotCommitId = 8;
        commitsListService.loadCommitsInContext(pivotCommitId).then(function(commits) {
            commitsList = commits;
            commitsListService.loadPreviousCommits();
        });
        $httpBackend.flush();

        // then
        var allLoadedCommitsCount = firstCommitsResponse.commits.length + previousCommitsResponse.commits.length;
        expect(commitsList.length).toBe(allLoadedCommitsCount);
    });

    it('should not call server to load next/previous commits when no next/previous available', function() {
        // given
        var commitsResponseWithNoNextPrev = buildCommitsResponse([4, 5, 6], 30);
        $httpBackend.expectGET('rest/commits?context=true&id=5&limit=7').respond(commitsResponseWithNoNextPrev);

        // when
        var pivotCommitId = 5;
        commitsListService.loadCommitsInContext(pivotCommitId).then(function() {
            commitsListService.loadNextCommits();
            commitsListService.loadPreviousCommits();
        });
        $httpBackend.flush();

        // then
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('should mark commit as reviewed and not delete from local list when in all mode', function() {
        // given
        var commitsResponse = buildCommitsResponse([1, 2, 3, 4, 5, 6, 7, 8], 30);
        $httpBackend.expectGET('rest/commits?context=true&limit=7').respond(commitsResponse);
        $httpBackend.expectDELETE('rest/commits/3').respond(commitsResponse);
        var commitIdToReview = 3;

        // when
        var commits;
        commitsListService.loadCommitsInContext().then(function(list) {
            commits = list;
            return commitsListService.makeReviewedAndGetNext(commitIdToReview);
        }).then(function() {
            expect(commits.length).toBe(commitsResponse.commits.length);
            var reviewed = commits.filter(function(commit) {
                return commit.pendingReview === false;
            });
            expect(reviewed.length).toBe(1);
            expect(reviewed[0].id).toBe(3);
        });
        $httpBackend.flush();
    });

    it('should mark commit as reviewed and delete from local list when in to_revoew mode', function() {
        var commitsResponse = buildCommitsResponse([1, 2, 3, 4, 5, 6, 7, 8], 30);
        $httpBackend.expectGET('rest/commits?filter=to_review&limit=7').respond(commitsResponse);
        $httpBackend.expectDELETE('rest/commits/3').respond(commitsResponse);
        var commitIdToReview = 3;

        // when
        var commits;
        commitsListService.loadCommitsToReview().then(function(list) {
            commits = list;
            return commitsListService.makeReviewedAndGetNext(commitIdToReview);
        }).then(function() {
            expect(commits.length).toBe(commitsResponse.commits.length - 1);
            var reviewed = commits.filter(function(commit) {
                return commit.pendingReview === false;
            });
            expect(reviewed.length).toBe(0);
        });
        $httpBackend.flush();
    });

    it('should load newest commits and mark only previous commits are available', function() {
        // given
        var commitsResponse = buildCommitsResponse([1, 2, 3, 4, 5, 6, 7], 30);
        $httpBackend.expectGET('rest/commits?context=true&limit=7').respond(commitsResponse);

        // when
        commitsListService.loadNewestCommits().then(function() {
            expect(commitsListService.hasNextCommits()).toBe(false);
            expect(commitsListService.hasPreviousCommits()).toBe(true);
        });
        $httpBackend.flush();
    });

    function buildCommitsResponse(commitsIds, totalCount) {
        var commits = commitsIds.map(function(id) {
            return {id: id, msg: 'Commit ' + id, pendingReview: true};
        });
        return {commits: commits, totalCount: totalCount};
    }

});
