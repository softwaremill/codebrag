angular.module('codebrag.branches')

    .controller('BranchesCtrl', function ($scope, branchesService, currentRepoContext, currentBranchCommitsCounter) {

        $scope.branches = branchesService.branches;
        $scope.showBranchesSelector = true;
        $scope.currentRepoContext = currentRepoContext;

        currentRepoContext.ready().then(function() {
            branchesService.loadBranches(currentRepoContext.repo);
        });

        branchesService.ready().then(function() {
            $scope.showBranchesSelector = (branchesService.repoType() === 'git');
        });

        $scope.selectBranch = function(selected) {
            currentRepoContext.switchBranch(selected.name);
        };

        $scope.isSelected = function(branch) {
            return currentRepoContext.branch === branch.name;
        };

        $scope.selectedBranch = function() {
            return currentRepoContext.branch;
        };

        $scope.displaySelectedMode = function() {
            return currentRepoContext.isToReviewFilterSet() ? $scope.toReviewLabel() : 'all'
        };

        $scope.toReviewLabel = function() {
            return 'to review (' + currentBranchCommitsCounter.toReviewCount + ')';
        };

        $scope.switchListView = function(mode) {
            currentRepoContext.switchCommitsFilter(mode);
        };

        $scope.toggleWatching = function(branch) {
            branchesService.toggleWatching(currentRepoContext.repo, branch);
        };

    });


