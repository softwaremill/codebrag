angular.module('codebrag.allfollowups')

    .controller('AllFollowupsCtrl', function ($scope, $http, allfollowupsService, pageTourService, events) {

        $scope.$on(events.allfollowupsTabOpened, initCtrl);


        $scope.pageTourForFollowupsVisible = function() {
            return pageTourService.stepActive('allfollowups') || pageTourService.stepActive('invites');
        };

        function initCtrl() {
            allfollowupsService.allFollowups().then(function(followups) {
                $scope.followupCommits = followups;
            });
            $scope.hasFollowupsAvailable = allfollowupsService.hasFollowups;
            $scope.mightHaveFollowups = allfollowupsService.mightHaveFollowups;
        }

        initCtrl();

    });
