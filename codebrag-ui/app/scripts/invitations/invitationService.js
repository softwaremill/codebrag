angular.module('codebrag.invitations')

    .factory('baseAppUrl', function($location) {
        return function() {
            var fullUrl = $location.absUrl();
            var appPath = $location.path();
            var endIndex = undefined;
            if(appPath !== '/') {
                endIndex = fullUrl.indexOf(appPath) + 1;
            }
            return fullUrl.slice(0, endIndex);
        }
    })

    .service('invitationService', function($http, $q, baseAppUrl) {

        var usersApiUrl = 'rest/users';

        this.loadRegisteredUsers = function() {
            return $http.get(usersApiUrl).then(function(response) {
                return response.data.users;
            });
        };

        this.loadInvitationLink = function() {
            var dfd = $q.defer();

            function success(response) {
                var invitationCode = response.data.invitationCode;
                var registrationUrl = baseAppUrl() + 'register/' + invitationCode;
                dfd.resolve(registrationUrl);
                return dfd.promise;
            }

            function error() {
                dfd.resolve('Sorry. We could not generate invitation link.');
                return dfd.promise;
            }

            return $http.get('rest/invitation').then(success, error);
        };

        this.sendInvitation = function(invitations, invitationLink) {
            var invitationRequestPayload = {
                emails: invitations.map(function(i) { return i.email; }),
                invitationLink: invitationLink
            };
            return $http.post('rest/invitation', invitationRequestPayload, {unique: true, requestId: 'invitation'});
        };

        this.validateEmails = function(emailsString) {
            var regexp = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
            var foundInvalid = emailsString.split(/[\s;,]+/).some(function (email) {
                return !(regexp.test(email));
            });
            return !foundInvalid;
        };

    });