describe("Branches Controller", function () {

    var $q, $scope, $controller, $rootScope;
    var branchesService, countersService, events;

    beforeEach(module('codebrag.branches'));

    beforeEach(inject(function (_$q_, _branchesService_, _countersService_, _$rootScope_, _$controller_, _events_) {
        $q = _$q_;
        $rootScope = _$rootScope_;
        $scope = _$rootScope_.$new();
        branchesService = _branchesService_;
        countersService = _countersService_;
        $controller = _$controller_;
        events = _events_;
    }));

    it('should load all branches on start', function() {
        // given
        var allBranches = ['master', 'feature', 'bugfix'];
        var allBranchesPromise = $q.when(allBranches);
        spyOn(branchesService, 'fetchBranches').andReturn(allBranchesPromise);

        // when
        $controller('BranchesCtrl', {$scope: $scope, branchesService: branchesService});
        $scope.$apply();

        // then
        expect($scope.branches).toBe(allBranches);
    });

    it('should change current branch', function() {
        // given
        var allBranches = ['master', 'feature', 'bugfix'];
        var allBranchesPromise = $q.when(allBranches);
        spyOn(branchesService, 'fetchBranches').andReturn(allBranchesPromise);
        spyOn(branchesService, 'selectBranch');

        // when
        $controller('BranchesCtrl', {$scope: $scope, branchesService: branchesService});
        $scope.$apply();
        $scope.selectBranch("feature");

        // then
        expect(branchesService.selectBranch).toHaveBeenCalledWith("feature");
    });

    it('should return correct label (with counter) for branch and commits to review', function() {
        // given
        spyOn(countersService.commitsCounter, 'currentCount').andReturn(20);
        $controller('BranchesCtrl', {$scope: $scope});

        // when
        var toReviewLabel = $scope.displaySelectedMode();
        $scope.switchListView('all');
        var allLabel = $scope.displaySelectedMode();

        // then
        expect(toReviewLabel).toBe('to review (20)');
        expect(allLabel).toBe('all');
    });
});
