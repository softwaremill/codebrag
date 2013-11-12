angular.module('codebrag.followups')

    .controller('FollowupsCtrl', function ($scope, $http, followupsService, pageTourService) {

        $scope.followupCommits = followupsService.allFollowups();
        $scope.hasFollowupsAvailable = followupsService.hasFollowups;
        $scope.mightHaveFollowups = followupsService.mightHaveFollowups;

        $scope.pageTourForFollowupsVisible = function() {
            return pageTourService.stepActive('followups');
        }

    });