angular.module('codebrag')

    .controller('HomeCtrl', function($scope, $state, currentRepoContext) {
        currentRepoContext.ready().then(function () {
            $state.transitionTo('commits.list', {repo: currentRepoContext.repo});
        })
    });
