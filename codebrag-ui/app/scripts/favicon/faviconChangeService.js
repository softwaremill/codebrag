angular.module('codebrag.favicon')

    .directive('tabNotifier', function($window, countersService) {

        var REGULAR_FAVICON = 'assets/images/favicon.ico';
        var NOTIFY_FAVICON = 'assets/images/notification-favicon/favicon.ico';

        var REGULAR_TITLE = 'Codebrag';
        var NOTIFY_TITLE = '* Codebrag';

        return {
            restrict: 'A',
            link: function(scope) {
                scope.$watch(updates, function(value) {
                    value === true ? setFavicon(NOTIFY_FAVICON, NOTIFY_TITLE) : setFavicon(REGULAR_FAVICON, REGULAR_TITLE);
                });

                function updates() {
                    return countersService.commitsCounter.updateAvailable() || countersService.followupsCounter.updateAvailable();
                }

                function setFavicon(iconUrl, tabTitle) {
                    $window.favicon && $window.favicon.change(iconUrl, tabTitle);
                }
            }
        }
    });