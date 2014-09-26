angular.module('codebrag.registration')

    /*
    Service that inherits from branches service.
    Uses the same logic but calls different urls and adds ability to load repos
     */

    .factory('initialWatchesSetupService', function(branchesService, registrationWizardData, $http, $q) {

        var service = Object.create(branchesService);

        service.urls = {
            baseUrl: function(repoName) {
                return 'rest/register/repos/' + repoName + '/branches';
            },
            getRepoBranches: function (repoName) {
                return this.baseUrl(repoName) + '?invitationCode=' + registrationWizardData.invitationCode + '&userId=' + registrationWizardData.registeredUser.id;
            },
            toggleWatchBranch: function (repoName, branchName) {
                return this.baseUrl(repoName) + '/' + branchName + '/watch' + '?invitationCode=' + registrationWizardData.invitationCode + '&userId=' + registrationWizardData.registeredUser.id;
            }
        };

        service.repos = [];
        service.loadRepos = function() {
            return $http.get('rest/register/repos?invitationCode=' + registrationWizardData.invitationCode).then(
                function success(response) {
                    Array.prototype.push.apply(service.repos, response.data);
                },
                function error(response) {
                    return $q.reject(response.data.errors);
                });
        };

        return service;
    });
