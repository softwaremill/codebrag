describe("Branches Controller", function () {

    var $q, $scope, $controller,
        branchesService, events, RepoBranch,
        allBranches = ['master', 'feature', 'bugfix'];

    beforeEach(module('codebrag.branches', 'codebrag.counters'));

    beforeEach(inject(function($injector) {
        $q = $injector.get('$q');
        $scope = $injector.get('$rootScope').$new();
        $controller = $injector.get('$controller');
        RepoBranch = $injector.get('RepoBranch');
        events = $injector.get('events');
    }));

    beforeEach(function() {
        branchesService = {
            branches: [],
            loadBranches: function() { return $q.when() },
            ready: function() { return $q.when() },
            repoType: function() { return 'git' }
        };
    });

    it('should load all branches on start', function() {
        // given
        var countersService = {},
            currentRepoContext = {};

        // when
        createController($scope, branchesService, countersService, currentRepoContext);
        $scope.$digest();

        // then
        expect($scope.branches).toEqual(branchesService.branches);
    });

    it('should change current branch and commits list filter', function() {
        // given
        var countersService = {},
            currentRepoContext = jasmine.createSpyObj('currentRepo', ['switchBranch', 'switchCommitsFilter']);

        createController($scope, branchesService, countersService, currentRepoContext);

        // when
        var selected = new RepoBranch({branchName: "feature"});
        $scope.selectBranch(selected);
        $scope.switchListView("all");

        // then
        expect(currentRepoContext.switchBranch).toHaveBeenCalledWith(selected.name);
        expect(currentRepoContext.switchCommitsFilter).toHaveBeenCalledWith("all");
    });

    it('should return correct label (with counter) for branch and commits to review', inject(function(Counter) {
        // given
        var currentRepoContext, countersService;

        currentRepoContext = {
            commitsFilter: 'pending',
            isToReviewFilterSet: function() {
                return true
            }
        };
        countersService = {
            commitsCounter: new Counter(10)
        };

        createController($scope, branchesService, countersService, currentRepoContext);

        // when
        var toReviewLabel = $scope.displaySelectedMode();

        // then
        expect(toReviewLabel).toBe('to review (10)');
    }));

    function createController(scope, branchesService, countersService, currentRepoContext) {
        $controller('BranchesCtrl', {
            $scope: scope,
            branchesService: branchesService,
            countersService: countersService,
            currentRepoContext: currentRepoContext
        });
    }
});
