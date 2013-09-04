angular.module('codebrag.session')

    .directive('showOnDemo', function(configService) {
        return {
            restrict: 'A',
            link: function(scope, el) {
                el.hide();
                configService.fetchConfig().then(function(config) {
                    !!config.demo ? el.show() : el.hide();
                });
            }
        }
    });