describe("Branches service", function () {

    var $httpBackend, $q, $rootScope;
    var branchesService, events;

    beforeEach(module('codebrag.branches'));

    beforeEach(inject(function (_$httpBackend_, _$q_, _branchesService_, _$rootScope_, _events_) {
        $httpBackend = _$httpBackend_;
        $q = _$q_;
        $rootScope = _$rootScope_;
        branchesService = _branchesService_;
        events = _events_;

        // TODO: create dedicated object for logged in user in rootScope
        $rootScope.loggedInUser = $rootScope.loggedInUser || {};
        $rootScope.loggedInUser.settings = $rootScope.loggedInUser.settings || {};
        $rootScope.loggedInUser.settings.selectedBranch = 'bugfix';
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should load available branches from server', function() {
        // given
        var expectedBranchesList;
        var allBranches = ['master', 'feature', 'bugfix'];
        $httpBackend.whenGET('rest/branches').respond({branches: allBranches});

        // when
        branchesService.fetchBranches();
        $httpBackend.flush();

        // then
        branchesService.allBranches().then(function(result) {
            expectedBranchesList = result;
        });
        $rootScope.$apply();
        expect(expectedBranchesList).toEqual(allBranches);
    });

    it('should select branch from user settings', function() {
        // given
        $httpBackend.whenGET('rest/branches').respond({branches: ['master', 'feature', 'bugfix']});

        // when
        branchesService.fetchBranches();
        $httpBackend.flush();

        // then
        expect(branchesService.selectedBranch()).toBe('bugfix');
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
        expect(expectedBranchesList).toEqual(allBranches);
    });

    it('should change currently selected branch', function() {
        // given
        spyOn($rootScope, '$broadcast').andCallThrough();
        var allBranches = ['master', 'feature', 'bugfix'];
        var branchesResponse = {branches: allBranches, current: 'master'};
        $httpBackend.expectGET('rest/branches').respond(branchesResponse);
        branchesService.fetchBranches();
        $httpBackend.flush();

        // when
        branchesService.selectBranch("feature");

        // then
        expect(branchesService.selectedBranch()).toBe('feature');
        expect($rootScope.$broadcast).toHaveBeenCalledWith(events.branches.branchChanged, 'feature');
    });

});
