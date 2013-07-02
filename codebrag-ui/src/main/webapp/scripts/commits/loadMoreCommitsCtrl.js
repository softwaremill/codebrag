angular.module('codebrag.commits')

    .controller('LoadMoreCommitsCtrl', function($scope, $rootScope, events, commitsListService, $q) {

        assertCommitsPresent();

        $scope.$on(events.commitCountChanged, function(event, data) {
            $scope.moreCommitsAvailable = canUserLoadMore(data.commitCount, $scope.commits);
        });

        $scope.loadMoreCommits = function () {
            commitsListService.loadMoreCommits().then(function() {
                $rootScope.$broadcast(events.moreCommitsLoaded);
            });
        };

        function canUserLoadMore(totalCount, currentlyLoadedList) {
            return $q.when(currentlyLoadedList).then(function(val) {
                return totalCount > val.length;
            })
        }

        function assertCommitsPresent() {
            !$scope.commits && (function () {
                throw 'Commits list promise not available'
            })();
        }
    });