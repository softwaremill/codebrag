'use strict';

angular.module('codebrag.followups')

    .controller('FollowupsCtrl', function ($scope, $http, followupsListService) {

        followupsListService.loadFollowupsFromServer();

        $scope.followups = followupsListService.allFollowups();

    });