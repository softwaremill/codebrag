angular.module('codebrag.notifications')

    .directive('dynamicFavicon', function(events) {

        return {
            restrict: 'A',
            link: function(scope, el, attrs) {

                scope.$on(events.updatesWaiting, function() {
                    setNotifyingFavicon();
                });

                [events.commitCountChanged, events.followupCountChanged].forEach(function(event) {
                    scope.$on(event, function() {
                        setRegularFavicon();
                    });
                });

                function setNotifyingFavicon() {
                    el.attr('href', attrs.notifyingIcon || 'assets/images/notification-favicon/favicon.ico');
                }

                function setRegularFavicon() {
                    el.attr('href', attrs.regularIcon || 'assets/images/favicon.ico');
                }
            }
        }

    });