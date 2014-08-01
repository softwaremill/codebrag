angular.module('codebrag.branches')

    .controller('BranchesCtrl', function ($scope, branchesService, events, countersService, currentRepoContext) {

        $scope.showBranchesSelector = true;
        $scope.currentRepoContext = currentRepoContext;

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
            return 'to review (' + countersService.commitsCounter.currentCount() + ')';
        };

        $scope.switchListView = function(mode) {
            currentRepoContext.switchCommitsFilter(mode);
        };

        $scope.toggleWatching = function(branch) {
            console.log('will toggle', branch.name, currentRepoContext.repo);
            branch.watching = !branch.watching;
        };

        branchesService.loadBranches().then(function(branchesList) {
            $scope.showBranchesSelector = (branchesService.repoType() === 'git');
            $scope.branches = branchesList;
        });

    });


