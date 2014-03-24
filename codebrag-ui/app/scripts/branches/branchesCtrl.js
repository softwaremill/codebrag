angular.module('codebrag.branches')

    .controller('BranchesCtrl', function ($rootScope, $scope, branchesService, events) {

        $scope.branches = [];

        branchesService.allBranches().then(function(list) {
            $scope.branches = list;
        });

        $scope.selectBranch = function(selected) {
            branchesService.selectBranch(selected);
            $rootScope.$broadcast(events.reloadCommitsList);
        };

        $scope.isSelected = function(branch) {
            return branchesService.selectedBranch() === branch;
        };

    });


