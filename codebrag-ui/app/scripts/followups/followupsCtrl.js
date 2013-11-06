angular.module('codebrag.followups')

    .controller('FollowupsCtrl', function ($scope, $http, followupsService) {

        $scope.followupCommits = followupsService.allFollowups();
        $scope.hasFollowupsAvailable = followupsService.hasFollowups;
        $scope.mightHaveFollowups = followupsService.mightHaveFollowups;

    });