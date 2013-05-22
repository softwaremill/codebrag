angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $http, commitsListService, commitLoadFilter) {

        $scope.filter = {

        };

        $scope.filter = {
            current: {
                value: 'pending'
            },

            setPendingMode: function() {
                this.current.value = 'pending';
            },

            setAllMode: function() {
                this.current.value = 'all';
            },

            isInPendingMode: function() {
                return this.current.value === 'pending';
            },

            isInAllMode: function() {
                return this.current.value === 'all';
            }
        };

        $scope.syncCommits = commitsListService.syncCommits;

        function loadCommits() {
            commitsListService.loadCommitsFromServer($scope.filter.current);
            $scope.commits = commitsListService.allCommits();
        }

        $scope.filterList = function(value) {
            if(filter.is)
            $scope.filter.setPendingMode();
            loadCommits();
        };

        $scope.showAll = function() {
            $scope.filter.setAllMode();
            loadCommits();
        };

        loadCommits();

    })

    .directive('styleAsSelectedWhen', function() {

        return {
            restrict: 'A',
            link: function(scope, el, attrs) {

                scope.$watch(attrs.styleAsSelectedWhen, function(selected) {
                    if(selected) {
                        el.removeClass('link');
                    } else {
                        el.addClass('link');
                    }
                });
            }
        }
    });