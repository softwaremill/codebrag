'use strict';

angular.module('codebrag.commits.followups')

    .controller('FollowupsCtrl', function ($location, $scope, $http, Followups, currentCommit) {

        $scope.currentCommit = currentCommit;

        Followups.get(function(responseData) {
            $scope.followups = responseData.followups;
        });
    });