describe("Branches service", function () {

    var $httpBackend, $q, $rootScope;
    var branchesService;

    beforeEach(module('codebrag.branches'));

    beforeEach(inject(function (_$httpBackend_, _$q_, _branchesService_, _$rootScope_) {
        $httpBackend = _$httpBackend_;
        $q = _$q_;
        $rootScope = _$rootScope_;
        branchesService = _branchesService_;
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('load available branches and current branch from server', function() {
        // given
        var expectedBranchesList;
        var allBranches = ['master', 'feature', 'bugfix'];
        var branchesResponse = {branches: allBranches, current: 'master'};
        $httpBackend.whenGET('rest/branches').respond(branchesResponse);

        // when
        branchesService.fetchBranches();
        $httpBackend.flush();

        // then
        expect(branchesService.selectedBranch()).toBe('master');
        branchesService.allBranches().then(function(result) {
            expectedBranchesList = result;
        });
        $rootScope.$apply();
        expect(expectedBranchesList).toBe(allBranches);
    });

    it('get available branches locally if they were previously loaded', function() {
        // given
        var expectedBranchesList;
        var allBranches = ['master', 'feature', 'bugfix'];
        var branchesResponse = {branches: allBranches, current: 'master'};
        $httpBackend.expectGET('rest/branches').respond(branchesResponse);
        branchesService.fetchBranches();
        $httpBackend.flush();

        // when
        var allBranchesPromise = branchesService.allBranches();

        // then
        allBranchesPromise.then(function(result) {
            expectedBranchesList = result;
        });
        $rootScope.$apply();
        expect(expectedBranchesList).toBe(allBranches);
    });

    it('should change currently selected branch', function() {
        // given
        var allBranches = ['master', 'feature', 'bugfix'];
        var branchesResponse = {branches: allBranches, current: 'master'};
        $httpBackend.expectGET('rest/branches').respond(branchesResponse);
        branchesService.fetchBranches();
        $httpBackend.flush();

        // when
        branchesService.selectBranch("feature");

        // then
        expect(branchesService.selectedBranch()).toBe('feature');

    });

});
