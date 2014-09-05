angular.module('codebrag.branches')

/*
Keeps to review commits count for current repo/branch.
Updates dynamically when commits are loaded, branch changed, commit is reviewed etc.
*/

    .factory('currentBranchCommitsCounter', function($rootScope, branchesService, currentRepoContext, events) {

        var counter = { toReviewCount: 0 };

        branchesService.ready().then(function() {
            bindEventListeners();
            reloadCounter();
        });

        function bindEventListeners() {
            $rootScope.$on(events.branches.branchChanged, reloadCounter);
            $rootScope.$on(events.commitsListFilterChanged, reloadCounter);
            $rootScope.$on(events.profile.emailAliasesChanged, reloadCounter);
            $rootScope.$on(events.commitReviewed, reloadCounter);
            $rootScope.$on(events.nextCommitsLoaded, reloadCounter);
            $rootScope.$on(events.previousCommitsLoaded, reloadCounter);
        }

        function reloadCounter() {
            var current = currentRepoContext;
            branchesService.loadBranchCommitsToReviewCount(current.repo, current.branch).then(function(count) {
                counter.toReviewCount = count;
            });
        }

        return counter;

    });

