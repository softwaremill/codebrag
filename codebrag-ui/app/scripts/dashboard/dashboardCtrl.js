angular.module('codebrag.dashboard')

    .controller('DashboardCtrl', function ($scope, $http, allFollowupsService, pageTourService, events) {

        $scope.$on(events.allfollowupsTabOpened, initCtrl);


        $scope.pageTourForFollowupsVisible = function() {
            return pageTourService.stepActive('dashboard') || pageTourService.stepActive('invites');
        };

        function initCtrl() {
            allFollowupsService.allFollowups().then(function(followups) {
                $scope.followupCommits = followups;
            });
            $scope.hasFollowupsAvailable = allFollowupsService.hasFollowups;
            $scope.mightHaveFollowups = allFollowupsService.mightHaveFollowups;
        }

        initCtrl();

    });
