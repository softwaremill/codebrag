angular.module('codebrag.tour')

    .controller('PageTourCtrl', function($scope, pageTourService, $state) {

        $scope.stepActive = function(stepName) {
            return pageTourService.stepActive(stepName);
        };

        $scope.ackStep = function(stepName) {
            pageTourService.ackStep(stepName);
            if(stepName === 'invites') {
                pageTourService.finishTour();
            }
        };

        $scope.inFollowups = function() {
            return $state.current.name === 'followups.list';
        };

        $scope.inCommits = function() {
            return $state.current.name === 'commits.list';
        };

    });

