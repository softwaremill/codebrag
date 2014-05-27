angular.module('codebrag.tour')

    .controller('PageTourCtrl', function($scope, pageTourService, $state, $rootScope, popupsService) {

        $scope.stepActive = function(stepName) {
            return pageTourService.stepActive(stepName);
        };

        $scope.ackStep = function(stepName) {
            pageTourService.ackStep(stepName);
        };

        $scope.inFollowups = function() {
            return $state.current.name === 'followups.list';
        };

        $scope.inCommits = function() {
            return $state.current.name === 'commits.list';
        };

        $scope.ackAndOpenInvitePopup = function(stepName) {
            $scope.ackStep(stepName);
            popupsService.openInvitePopup();
        }

    });

