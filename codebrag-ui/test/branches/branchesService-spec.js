describe("Branches service", function () {

    var $httpBackend, $q, $rootScope;
    var branchesService,
        currentRepoContext = {
            repo: 'codebrag'
        },
        events;

    beforeEach(module('codebrag.branches', function($provide) {
        $provide.factory('currentRepoContext', function() {
            return currentRepoContext;
        });
    }));

    beforeEach(inject(function (_$httpBackend_, _$q_, _branchesService_, _currentRepoContext_, _$rootScope_, _events_) {
        $httpBackend = _$httpBackend_;
        $q = _$q_;
        $rootScope = _$rootScope_;
        branchesService = _branchesService_;
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
        var allBranches = ['master', 'feature', 'bugfix'];
        $httpBackend.whenGET('rest/branches?repo=codebrag').respond({branches: allBranches});

        // when
        branchesService.loadBranches().then(function(branches) {
            expectedBranchesList = branches;
        });
        $httpBackend.flush();

        // then
        expect(expectedBranchesList).toEqual(allBranches);
    });

});
