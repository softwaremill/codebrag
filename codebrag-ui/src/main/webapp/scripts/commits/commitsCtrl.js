angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $http, commitsListService) {

        commitsListService.loadCommitsFromServer();

        $scope.commits = commitsListService.allCommits();

        $scope.syncCommits = commitsListService.syncCommits;

    });