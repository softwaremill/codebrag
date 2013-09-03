angular.module('codebrag.session')

    .directive('showOnDemo', function(configService) {
        return {
            restrict: 'A',
            scope: {},
            link: function(scope, el) {
                el.hide();
                configService.fetchConfig().then(function(config) {
                    !!config.demo ? el.show() : el.hide();
                });
            }
        }
    });