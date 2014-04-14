describe("Branches Controller", function () {

    var $q, $scope, $controller, $rootScope;
    var branchesService, events;

    beforeEach(module('codebrag.branches'));

    beforeEach(inject(function (_$q_, _branchesService_, _$rootScope_, _$controller_, _events_) {
        $q = _$q_;
        $rootScope = _$rootScope_;
        $scope = _$rootScope_.$new();
        branchesService = _branchesService_;
        $controller = _$controller_;
        events = _events_;
    }));

    it('should load all branches on start', function() {
        // given
        var allBranches = ['master', 'feature', 'bugfix'];
        var allBranchesPromise = $q.when(allBranches);
        spyOn(branchesService, 'allBranches').andReturn(allBranchesPromise);

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
        spyOn(branchesService, 'allBranches').andReturn(allBranchesPromise);
        spyOn(branchesService, 'selectBranch');

        // when
        $controller('BranchesCtrl', {$scope: $scope, branchesService: branchesService});
        $scope.$apply();
        $scope.selectBranch("feature");

        // then
        expect(branchesService.selectBranch).toHaveBeenCalledWith("feature");
    });

});
