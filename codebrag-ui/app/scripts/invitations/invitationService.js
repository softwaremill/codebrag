angular.module('codebrag.invitations')

    .service('invitationService', function($http, $q) {

        this.loadRegisteredUsers = function() {
            return $http.get('rest/users/all').then(function(response) {
                return response.data.registeredUsers;
            });
        };

        this.loadInvitationLink = function() {
            var dfd = $q.defer();

            function success(response) {
                dfd.resolve(response.data.invitationLink);
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
        }

        this.validateEmails = function(emailsString) {
            var foundInvalid = emailsString.split(/[\s;,]+/).some(function (email) {
                return !(/^\w+@[a-zA-Z_]+?\.[a-zA-Z]{2,3}$/.test(email));
            });
            return !foundInvalid;
        }

    });
