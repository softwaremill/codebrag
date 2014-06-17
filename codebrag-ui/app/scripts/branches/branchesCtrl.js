angular.module('codebrag.branches')

    .controller('BranchesCtrl', function ($scope, branchesService, events, countersService, currentRepoContext) {

        $scope.showBranchesSelector = false;
        $scope.current = currentRepoContext;

        $scope.selectBranch = function(selected) {
            currentRepoContext.switchBranch(selected);
        };

        $scope.isSelected = function(branch) {
            return currentRepoContext.branch === branch;
        };

        $scope.selectedBranch = function() {
            return currentRepoContext.branch;
        };

        $scope.displaySelectedMode = function() {
            return currentRepoContext.isToReviewFilterSet() ? $scope.toReviewLabel() : 'all'
        };

        $scope.toReviewLabel = function() {
            return 'to review (' + countersService.commitsCounter.currentCount() + ')';
        };

        $scope.switchListView = function(mode) {
            currentRepoContext.switchCommitsFilter(mode);
        };

        branchesService.loadBranches().then(function(branchesList) {
            $scope.showBranchesSelector = (branchesService.repoType() === 'git');
            $scope.branches = branchesList;
        });

    });


