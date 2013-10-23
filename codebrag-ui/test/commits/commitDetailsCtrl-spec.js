'use strict';

describe("CommitDetailsController", function () {

    var selectedCommitId = 123;
    var noopPromise = {then: function(){}};
    var $scope, $q, commitsService;

    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function($rootScope, _$q_, _commitsService_) {
        $scope = $rootScope.$new();
        $q = _$q_;
        commitsService = _commitsService_;
    }));

    it('should use commit id provided in $stateParams to load commit data', inject(function($stateParams, $controller) {
        // Given
        $stateParams.id = selectedCommitId;
        spyOn(commitsService, 'commitDetails').andReturn(noopPromise);

        // when
        $controller('CommitDetailsCtrl', {$scope: $scope, commitsListService: commitsService});

        // Then
        expect(commitsService.commitDetails).toHaveBeenCalledWith(selectedCommitId);
    }));

    it('should expose loaded commit in scope', inject(function($controller, $stateParams) {
        // Given
        $stateParams.id = selectedCommitId;
        var expectedCommitDetails = {commit: {sha: '123'}, diff: [], supressedFiles: []};
        var commitDetails = $q.defer();
        commitDetails.resolve(expectedCommitDetails);
        spyOn(commitsService, 'commitDetails').andReturn(commitDetails.promise);

        // When
        $controller('CommitDetailsCtrl', {$scope: $scope, commitsListService: commitsService});
        $scope.$apply();

        //then
        expect($scope.currentCommit.info).toBe(expectedCommitDetails.commit);
    }));

});
