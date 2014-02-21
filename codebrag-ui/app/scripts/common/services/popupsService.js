angular.module('codebrag.common.services')

    .service('popupsService', function($modal) {

        function modalConfig(config) {
            var defaultConfig = {
                backdrop: false,
                keyboard: true
            };
            return angular.extend(defaultConfig, config);
        }

        this.openAboutPopup = function() {
            var aboutModalConfig = {
                templateUrl: 'views/popups/about.html',
                controller: 'AboutCtrl'
            };
            return $modal.open(modalConfig(aboutModalConfig));
        };

        this.openRepoNotReadyPopup = function(repoState) {
            var repoStatusModalConfig = {
                keyboard: false,
                templateUrl: 'views/popups/repoNotReady.html',
                controller: 'RepositoryStatusCtrl',
                resolve: {
                    repoStatus: function () {
                        return repoState;
                    }
                }
            };
            return $modal.open(modalConfig(repoStatusModalConfig));
        };

        this.openInvitePopup = function() {
            var inviteModalConfig = {
                templateUrl: 'views/popups/inviteForm.html'
            };
            return $modal.open(modalConfig(inviteModalConfig));
        };

    });