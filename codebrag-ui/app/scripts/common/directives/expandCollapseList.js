angular.module('codebrag.common.directives')

    .directive('expandCollapseList', function($window, expandCollapseListService) {
        return {
            restrict: 'A',
            link: function(scope, el) {
                el.on('click', function() {
                    expandCollapseListService.toggle()
                });
            }
        };
    });