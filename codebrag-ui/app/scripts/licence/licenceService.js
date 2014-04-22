angular.module('codebrag.licence')

    .service('licenceService', function($http, $q, $rootScope, events, $timeout, $modal) {

        var warningDays = 14,
            licenceData = {},
            ready = $q.defer(),
            checkTimer,
            checkInterval = 6 * 3600 * 1000; // 6 hours (in millis)

        function scheduleLicenceCheck() {
            return loadLicenceData().then(scheduleNextCheck).then(fireEvents).then(function() {
                ready.resolve();
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
                licenceData = response.data;
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
                $rootScope.$broadcast('codebrag:licenceAboutToExpire');
            }
            if(!licenceData.valid) {
                $rootScope.$broadcast('codebrag:licenceExpired');
            }
        }

        function initialize() {
            $rootScope.$on(events.loggedIn, scheduleLicenceCheck);

            $rootScope.$on('codebrag:openLicencePopup', licencePopup);
            $rootScope.$on('codebrag:licenceExpired', function() {
                if(angular.isUndefined(licencePopup.initialDisplay)) {
                    licencePopup();
                    licencePopup.initialDisplay = true;
                }
            });
        }

        function licencePopup() {
            var repoStatusModalConfig = {
                backdrop: false,
                keyboard: true,
                controller: 'LicenceInfoCtrl',
                resolve: {
                    licenceData: serviceReady
                },
                templateUrl: 'views/popups/licenceInfo.html'
            };
            return $modal.open(repoStatusModalConfig);
        }

        return {
            ready: serviceReady,
            getLicenceData: getLicenceData,
            licencePopup: licencePopup,
            initialize: initialize
        };

    });