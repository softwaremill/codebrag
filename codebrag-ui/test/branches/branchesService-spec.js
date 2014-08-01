describe("Branches service", function () {

    var $httpBackend, $q, $rootScope;
    var branchesService,
        RepoBranch,
        currentRepoContext = {
            repo: 'codebrag'
        },
        events;

    beforeEach(module('codebrag.branches', function($provide) {
        $provide.factory('currentRepoContext', function() {
            return currentRepoContext;
        });
    }));

    beforeEach(inject(function (_$httpBackend_, _$q_, _branchesService_, _RepoBranch_, _currentRepoContext_, _$rootScope_, _events_) {
        $httpBackend = _$httpBackend_;
        $q = _$q_;
        $rootScope = _$rootScope_;
        branchesService = _branchesService_;
        RepoBranch = _RepoBranch_;
        currentRepoContext = _currentRepoContext_;
        events = _events_;

        $rootScope.loggedInUser = {
            settings: {
                selectedBranch: 'bugfix'
            }
        };
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should load available branches for given repo from server', function() {
        // given
        currentRepoContext.repo = 'codebrag';
        var expectedBranchesList;
        var allBranches = [{branchName: 'master', watching: false}, {branchName: 'feature', watching: true}];
        $httpBackend.whenGET('rest/repos/codebrag/branches').respond({branches: allBranches});

        // when
        branchesService.loadBranches().then(function(branches) {
            expectedBranchesList = branches;
        });
        $httpBackend.flush();

        // then
        var branches = allBranches.map(function(b) {
            return new RepoBranch(b);
        });
        expect(expectedBranchesList).toEqual(branches);
    });

});
