angular.module('codebrag.branches')

    .controller('BranchesCtrl', function ($rootScope, $scope, branchesService, events) {

        $scope.branches = [];

        $scope.selectBranch = function(selected) {
            branchesService.selectBranch(selected);
        };

        $scope.isSelected = function(branch) {
            return branchesService.selectedBranch() === branch;
        };

        $scope.selectedBranch = branchesService.selectedBranch;

        function init() {
            branchesService.fetchBranches().then(function(list) {
                $scope.branches = list;
            });
        }

        init();

        $scope.$on(events.commitsTabOpened, init);

    });


