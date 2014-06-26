angular.module('codebrag.branches')

    .controller('ReposCtrl', function ($scope, $state, events, currentRepoContext) {

        $scope.repos = function() {
            return Object.getOwnPropertyNames(currentRepoContext.all);
        };

        $scope.selectRepo = function(selected) {
            currentRepoContext.switchRepo(selected);
            $state.transitionTo('commits.list', {repo: selected});
        };

        $scope.isSelected = function(repo) {
            return currentRepoContext.repo === repo;
        };

        $scope.selectedBranch = function() {
            return currentRepoContext.repo;
        };

    });


