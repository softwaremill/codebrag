angular.module('codebrag.commits')

    .directive('scrollCommitsList', function(events) {

        var idToScrollTo = '#commits-list-end';

        return {
            restrict: 'A',
            link: function(scope, el) {
                scope.$on(events.moreCommitsLoaded, function() {
                    var options = {
                        duration: 1000,
                        easing:'easeOutCirc'
                    };
                    el.scrollTo(el.find(idToScrollTo), options);
                })
            }
        }
    });