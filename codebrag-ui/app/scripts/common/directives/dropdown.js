angular.module('codebrag.common.directives').directive('dropdown', function() {

    return {
        restrict: 'E',
        scope: {
            selected: '=',
            icon: '@'
        },
        templateUrl: 'views/dropdown.html',
        transclude: true,
        replace: true,
        link: function(scope, el) {
            var listItems = el.find('section');
            el.on('hover', function() {
                listItems.show();
            });
            el.on('click', function() {
                listItems.toggle();
            });
        }
    };

});