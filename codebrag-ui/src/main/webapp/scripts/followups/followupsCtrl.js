'use strict';

angular.module('codebrag.followups')

    .controller('FollowupsCtrl', function ($scope, $http, followupsListService) {

        $scope.followupCommits = followupsListService.loadFollowupGroupsFromServer();

    });