'use strict';

angular.module('codebrag.followups')

    .controller('FollowupsCtrl', function ($scope, $http, followupsListService) {

        $scope.followups = followupsListService.loadFollowupsFromServer();

    });