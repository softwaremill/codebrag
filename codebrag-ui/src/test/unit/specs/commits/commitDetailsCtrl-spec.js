'use strict';

describe("CommitDetailsController", function () {

    var selectedCommitId = 123;
    var noopPromise = {then: function(){}};
    var $scope, $q, commitsListService;

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function($rootScope, _$q_, _commitsListService_) {
        $scope = $rootScope.$new();
        $q = _$q_;
        commitsListService = _commitsListService_;
    }));

    var singleStoredComment = {id: '123', authorName: "mostr", message: "this is comment", time: "2013-03-29T15:14:10Z"};

    it('should use commit id provided in $stateParams to load commit data', inject(function($stateParams, $controller) {
        // Given
        $stateParams.id = selectedCommitId;
        spyOn(commitsListService, 'loadCommitDetails').andReturn(noopPromise);

        // when
        $controller('CommitDetailsCtrl', {$scope: $scope, commitsListService: commitsListService});

        // Then
        expect(commitsListService.loadCommitDetails).toHaveBeenCalledWith(selectedCommitId);
    }));

    it('should expose loaded commit in scope', inject(function($controller, $stateParams) {
        // Given
        $stateParams.id = selectedCommitId;
        var expectedCommitDetails = {commit: {sha: '123'}, diff: [], supressedFiles: []};
        var commitDetails = $q.defer();
        commitDetails.resolve(expectedCommitDetails);
        spyOn(commitsListService, 'loadCommitDetails').andReturn(commitDetails.promise);

        // When
        $controller('CommitDetailsCtrl', {$scope: $scope, commitsListService: commitsListService});
        $scope.$apply();

        //then
        expect($scope.currentCommit.info).toBe(expectedCommitDetails.commit);
    }));

    it('should call service to mark current commit as reviewed', inject(function($controller, $stateParams) {
        // Given
        $stateParams.id = selectedCommitId;
        spyOn(commitsListService, 'loadCommitDetails').andReturn(noopPromise);
        spyOn(commitsListService, 'makeReviewedAndGetNext').andReturn(noopPromise);

        // When
        $controller('CommitDetailsCtrl', {$scope: $scope});
        $scope.markCurrentCommitAsReviewed();

        // Then
        expect(commitsListService.makeReviewedAndGetNext).toHaveBeenCalledWith(selectedCommitId);
    }));

    it('should go to next commit when making current commit reviewed', inject(function($controller, $stateParams, $state) {
        // Given
        $stateParams.id = selectedCommitId;
        spyOn(commitsListService, 'loadCommitDetails').andReturn(noopPromise);
        var nextCommit = $q.defer();
        nextCommit.resolve({id: '345'});
        spyOn(commitsListService, 'makeReviewedAndGetNext').andReturn(nextCommit.promise);
        spyOn($state, 'transitionTo');

        // When
        $controller('CommitDetailsCtrl', {$scope: $scope});
        $scope.markCurrentCommitAsReviewed();
        $scope.$apply();

        // Then
        expect($state.transitionTo).toHaveBeenCalledWith('commits.details', {id: '345'});
    }));

    it('should go to commits list when no next commit available', inject(function($controller, $stateParams, $state) {
        // Given
        $stateParams.id = selectedCommitId;
        spyOn(commitsListService, 'loadCommitDetails').andReturn(noopPromise);
        var nextCommit = $q.defer();
        nextCommit.resolve(null);
        spyOn(commitsListService, 'makeReviewedAndGetNext').andReturn(nextCommit.promise);
        spyOn($state, 'transitionTo');

        // When
        $controller('CommitDetailsCtrl', {$scope: $scope});
        $scope.markCurrentCommitAsReviewed();
        $scope.$apply();

        // Then
        expect($state.transitionTo).toHaveBeenCalledWith('commits.list');
    }));

});
