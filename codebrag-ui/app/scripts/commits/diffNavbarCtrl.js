angular.module('codebrag.commits')

    .controller('DiffNavbarCtrl', function ($scope, currentCommit, commitsService, $state) {

        $scope.markCurrentCommitAsReviewed = function () {
            var shaToRemove = $scope.currentCommit.info.sha;
            currentCommit.empty();
            commitsService.markAsReviewed(shaToRemove).then(function(nextCommit) {
                goTo(nextCommit);
            })
        };

        var commitAvailable = function() {
            return currentCommit.get();
        };

        $scope.$watch(commitAvailable, function(commit) {
            $scope.currentCommit = commit;
            $scope.currentCommit && ($scope.readableCommitStatus = mapCommitStatus(commit));
        });

        function mapCommitStatus(commit) {
            if(angular.isUndefined(mapCommitStatus.statusMap)) {
                mapCommitStatus.statusMap = {
                    AwaitingUserReview: 'Mark commit as reviewed',
                    ReviewedByUser: 'Reviewed by me',
                    AwaitingOthersReview: 'Your commit - awaiting review',
                    ReviewedByOthers: 'Reviewed by others'
                };
            }
            return mapCommitStatus.statusMap[commit.info.state];
        }


        function goTo(nextCommit) {
            if (nextCommit) {
                return $state.transitionTo('commits.details', {sha: nextCommit.sha});
            }
            $state.transitionTo('commits.list');
        }

    });

