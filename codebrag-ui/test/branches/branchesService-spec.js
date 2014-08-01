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
        var allBranches = [{branchName: 'master', watching: false}, {branchName: 'feature', watching: true}];
        $httpBackend.whenGET('rest/repos/codebrag/branches').respond({branches: allBranches});

        // when
        branchesService.loadBranches();
        $httpBackend.flush();

        // then
        var branches = allBranches.map(function(b) {
            return new RepoBranch(b);
        });
        expect(branchesService.branches).toEqual(branches);
    });

    it('should toggle watching for branch', function() {
        // given
        currentRepoContext.repo = 'codebrag';
        branchesService.branches = [
            new RepoBranch({branchName: 'master', watching: false}),
            new RepoBranch({branchName: 'feature', watching: true})
        ];
        $httpBackend.expectPOST('rest/repos/codebrag/branches/master/watch').respond(200);

        // when
        var target = branchesService.branches[0];
        branchesService.toggleWatching(target);
        $httpBackend.flush();

        // then
        expect(target.watching).toBeTruthy();
    });

    it('should revert back watch for branch when error happened', function() {
        // given
        currentRepoContext.repo = 'codebrag';
        branchesService.branches = [
            new RepoBranch({branchName: 'master', watching: false}),
            new RepoBranch({branchName: 'feature', watching: true})
        ];
        $httpBackend.expectPOST('rest/repos/codebrag/branches/master/watch').respond(400);

        // when
        var target = branchesService.branches[0];
        branchesService.toggleWatching(target);
        $httpBackend.flush();

        // then
        expect(target.watching).toBeFalsy();
    });

});
