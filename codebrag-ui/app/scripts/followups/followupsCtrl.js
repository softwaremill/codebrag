angular.module('codebrag.followups')

    .controller('FollowupsCtrl', function ($scope, $http, followupsService, pageTourService, events) {

        $scope.$on(events.followupsTabOpened, initCtrl);


        $scope.pageTourForFollowupsVisible = function() {
            return pageTourService.stepActive('followups') || pageTourService.stepActive('invites');
        };

        function initCtrl() {
            followupsService.allFollowups().then(function(followups) {
                $scope.followupCommits = followups;
            });
            $scope.hasFollowupsAvailable = followupsService.hasFollowups;
            $scope.mightHaveFollowups = followupsService.mightHaveFollowups;
        }

        initCtrl();

    });