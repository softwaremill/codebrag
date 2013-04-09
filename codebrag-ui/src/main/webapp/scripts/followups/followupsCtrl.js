'use strict';

angular.module('codebrag.followups')

    .controller('FollowupsCtrl', function ($scope, $http, Followups) {

        Followups.get(function(responseData) {
            $scope.followups = responseData.followups;
        });

    });