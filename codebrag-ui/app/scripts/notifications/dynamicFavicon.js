angular.module('codebrag.notifications')

    .directive('dynamicFavicon', function(events) {

        return {
            restrict: 'A',
            link: function(scope, el, attrs) {

                scope.$on(events.updatesWaiting, function(event, data) {
                    checkUpdatesAvailable(data) ? setNotifyingFavicon() : setRegularFavicon();
                });

                scope.$on(events.resetNotifications, function() {
                    setRegularFavicon();
                });

                function checkUpdatesAvailable(data) {
                    var updatesPresent = false;
                    for (var p in data) {
                        if (data.hasOwnProperty(p) && data[p] > 0) {
                            updatesPresent = true;
                        }
                    }
                    return updatesPresent;
                }

                function setNotifyingFavicon() {
                    el.attr('href', attrs.notifyingIcon || 'assets/images/notification-favicon/favicon.ico');
                }

                function setRegularFavicon() {
                    el.attr('href', attrs.regularIcon || 'assets/images/favicon.ico');
                }
            }
        }

    });