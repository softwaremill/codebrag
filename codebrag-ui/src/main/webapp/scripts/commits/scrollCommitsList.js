angular.module('codebrag.commits')

    .directive('scrollCommitsList', function(events) {

        return {
            restrict: 'A',
            link: function(scope, el) {
                scope.$on(events.nextCommitsLoaded, function() {
                    el.animate({scrollTop: el[0].scrollHeight});
                });
                scope.$on(events.previousCommitsLoaded, function() {
                    el.animate({scrollTop: 0});
                });
            }
        };
    });