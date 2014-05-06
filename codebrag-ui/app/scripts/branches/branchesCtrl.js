angular.module('codebrag.branches')

    .controller('BranchesCtrl', function ($rootScope, $scope, branchesService, events, countersService) {

        var listFilter = 'pending';
        $scope.branches = [];
        $scope.showBranchesSelector = false;

        $scope.selectBranch = function(selected) {
            branchesService.selectBranch(selected);
            $rootScope.$broadcast(events.commitsListFilterChanged, listFilter);
        };

        $scope.isSelected = function(branch) {
            return branchesService.selectedBranch() === branch;
        };

        $scope.selectedBranch = function() {
            return branchesService.selectedBranch();
        };

        $scope.displaySelectedMode = function() {
            return listFilter === 'all' ? 'all' : $scope.toReviewLabel();
        };

        $scope.toReviewLabel = function() {
            return 'to review (' + countersService.commitsCounter.currentCount() + ')';
        };

        $scope.switchListView = function(mode) {
            listFilter = mode;
            $rootScope.$broadcast(events.commitsListFilterChanged, listFilter);
        };

        function init() {
            $scope.$on(events.commitsTabOpened, init);
            branchesService.fetchBranches().then(function(list) {
                $rootScope.$broadcast(events.commitsListFilterChanged, listFilter);
                $scope.showBranchesSelector = branchesService.repoType() === 'git' ? true : false;
                $scope.branches = list;
            });
        }

        init();

    });


