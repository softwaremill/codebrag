angular.module('codebrag.licence')

    .service('licenceService', function($http, $q, $rootScope, events, $timeout, $modal) {

        var warningMinutes = 62, // 14 * 24 * 60,
            licenceData = {},
            ready = $q.defer();

        function loadLicenceData() {
            return $http.get('rest/licence').then(function(response) {
                licenceData = response.data;
                ready.resolve(licenceData);
            });
        }

        function serviceReady() {
            return ready.promise;
        }

        function getLicenceData() {
            return licenceData;
        }

        function setupExpirationSoonEvent() {
            var triggerTime = (licenceData.minutesLeft - warningMinutes) * 60 * 1000;
            $timeout(function() {
                $rootScope.$broadcast('codebrag:licenceAboutToExpire')
            }, triggerTime);
        }

        function setupLicenceExpiredWarning() {
            var triggerTime = licenceData.minutesLeft * 60 * 1000;
            $timeout(function() {
                $rootScope.$broadcast('codebrag:licenceExpired');
            }, triggerTime);
        }

        function initialize() {
            $rootScope.$on(events.loggedIn, function() {
                loadLicenceData().then(function() {
                    if(licenceData.valid) {
                        setupExpirationSoonEvent();
                    }
                    setupLicenceExpiredWarning();
                });
            });

            $rootScope.$on('codebrag:openLicencePopup', licencePopup);
            $rootScope.$on('codebrag:licenceExpired', licencePopup);
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