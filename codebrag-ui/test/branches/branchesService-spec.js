describe("Branches service", function () {

    var $httpBackend, $q, $rootScope;
    var branchesService, RepoBranch, events;

    beforeEach(module('codebrag.branches'));

    beforeEach(inject(function (_$httpBackend_, _$q_, _branchesService_, _RepoBranch_, _$rootScope_, _events_) {
        $httpBackend = _$httpBackend_;
        $q = _$q_;
        $rootScope = _$rootScope_;
        branchesService = _branchesService_;
        RepoBranch = _RepoBranch_;
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
        var allBranches = [{branchName: 'master', watching: false}, {branchName: 'feature', watching: true}];
        $httpBackend.whenGET('rest/repos/codebrag/branches').respond({branches: allBranches});

        // when
        branchesService.loadBranches('codebrag');
        $httpBackend.flush();

        // then
        var branches = allBranches.map(function(b) {
            return new RepoBranch(b);
        });
        expect(branchesService.branches).toEqual(branches);
    });

    it('should toggle watching for branch', function() {
        // given
        branchesService.branches = [
            new RepoBranch({branchName: 'master', watching: false}),
            new RepoBranch({branchName: 'feature', watching: true})
        ];
        $httpBackend.expectPOST('rest/repos/codebrag/branches/master/watch').respond(200);

        // when
        var target = branchesService.branches[0];
        branchesService.toggleWatching('codebrag', target);
        $httpBackend.flush();

        // then
        expect(target.watching).toBeTruthy();
    });

    it('should revert back watch for branch when error happened', function() {
        // given
        branchesService.branches = [
            new RepoBranch({branchName: 'master', watching: false}),
            new RepoBranch({branchName: 'feature', watching: true})
        ];
        $httpBackend.expectPOST('rest/repos/codebrag/branches/master/watch').respond(400);

        // when
        var target = branchesService.branches[0];
        branchesService.toggleWatching('codebrag', target);
        $httpBackend.flush();

        // then
        expect(target.watching).toBeFalsy();
    });

    it('should load to review commits count for current repo/branch', function() {
        // given
        var response = { toReviewCount: 10 };
        $httpBackend.expectGET('rest/repos/codebrag/branches/master/count').respond(response);

        // when
        var expectedCount;
        branchesService.loadBranchCommitsToReviewCount('codebrag', 'master').then(function(count) {
            expectedCount = count;
        });
        $httpBackend.flush();

        // then
        expect(expectedCount).toBe(response.toReviewCount);
    })

});
