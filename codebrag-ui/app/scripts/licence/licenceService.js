angular.module('codebrag.licence')

    .service('licenceService', function($http, $q, $rootScope, events, $timeout, $modal) {

        var warningDays = 14,
            licenceData = {},
            ready = $q.defer(),
            checkTimer,
            checkInterval = 6 * 3600 * 1000; // 6 hours (in millis)

        function scheduleLicenceCheck() {
            return loadLicenceData().then(scheduleNextCheck).then(fireEvents).then(function() {
                ready.resolve(licenceData);
            });
            function scheduleNextCheck() {
                checkTimer && $timeout.cancel(checkTimer);
                checkTimer = $timeout(function() {
                    loadLicenceData().then(scheduleNextCheck).then(fireEvents)
                }, checkInterval);
            }
        }

        function loadLicenceData() {
            return $http.get('rest/licence').then(function(response) {
                angular.copy(response.data, licenceData);
                return licenceData;
            });
        }

        function serviceReady() {
            return ready.promise;
        }

        function getLicenceData() {
            return licenceData;
        }

        function fireEvents() {
            var daysToWarning = licenceData.daysLeft - warningDays;
            if(licenceData.valid && daysToWarning < 0) {
                $rootScope.$broadcast(events.licence.licenceAboutToExpire);
            }
            if(!licenceData.valid && !fireEvents.initialExpirationEvent) {
                $rootScope.$broadcast(events.licence.licenceExpired);
                fireEvents.initialExpirationEvent = true
            }
        }

        function initialize() {
            $rootScope.$on(events.loggedIn, scheduleLicenceCheck);
            $rootScope.$on(events.licence.openPopup, licencePopup);
            $rootScope.$on(events.licence.licenceExpired, licencePopup);
            $rootScope.$on(events.licence.licenceKeyRegistered, scheduleLicenceCheck);
        }

        function licencePopup(once) {
            if(licencePopup.displayed) return;
            var repoStatusModalConfig = {
                backdrop: false,
                keyboard: true,
                controller: 'LicenceInfoCtrl',
                resolve: {
                    licenceData: loadLicenceData
                },
                templateUrl: 'views/popups/licenceInfo.html'
            };
            licencePopup.displayed = true;
            $modal.open(repoStatusModalConfig).result.then(function() {
                licencePopup.displayed = false;
            }, function() {
                licencePopup.displayed = false;
            })
        }

        return {
            ready: serviceReady,
            getLicenceData: getLicenceData,
            loadLicenceData: loadLicenceData,
            licencePopup: licencePopup,
            initialize: initialize
        };

    });