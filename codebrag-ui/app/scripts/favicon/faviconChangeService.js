angular.module('codebrag.favicon')

    /**
     *  This service manipulates DOM
     *  It uses favicon.js to insert new <link> element to the DOM.
     *  This is because changing existing favicon doesn't work in FF (it needs to be removed and recreated)
     */
    .service('faviconChangeService', function($rootScope, events, $window) {

        this.setupNotificationWatchers = function() {
            setRegularFavicon();
            setupNotificationEventListeners();
        };

        function setupNotificationEventListeners() {
            $rootScope.$on(events.updatesWaiting, function(event, data) {
                checkUpdatesAvailable(data) ? setNotifyingFavicon() : setRegularFavicon();
            });
            $rootScope.$on(events.resetNotifications, function() {
                setRegularFavicon();
            });
        }

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
            $window.favicon && $window.favicon.change('assets/images/notification-favicon/favicon.ico', '* Codebrag');
        }

        function setRegularFavicon() {
            $window.favicon && $window.favicon.change('assets/images/favicon.ico', 'Codebrag');
        }

    });