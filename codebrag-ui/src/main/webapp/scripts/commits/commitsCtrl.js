angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $http, commitsListService) {

        $scope.syncCommits = commitsListService.syncCommits;

        $scope.loadAllCommits = function() {
            $scope.commits = commitsListService.loadAllCommits();
        };

        $scope.loadPendingCommits = function() {
            $scope.commits = commitsListService.loadCommitsPendingReview();
        };


        $scope.loadPendingCommits();

    })

    .directive('activateSingle', function() {
        var addClassAttribute = "toggleClass";
        return {
            restrict: 'A',
            link: function(scope, el, attrs) {
                el.on('click', attrs.activateSingle, function(event) {
                    el.find(attrs.activateSingle).toggleClass(attrs[addClassAttribute]);
                });
            }
        }
    });
