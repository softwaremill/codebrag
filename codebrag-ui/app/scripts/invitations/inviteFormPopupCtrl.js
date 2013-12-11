angular.module('codebrag.invitations')

    .controller('InviteFormPopupCtrl', function($scope, invitationService) {

        loadInvitationLink();
        loadRegisteredUsers();


        $scope.validateEmails = function(emailsString) {
            if(emailsString) {
                return invitationService.validateEmails(emailsString);
            }
        };

        $scope.submitOnEnter = function($event) {
            $event.preventDefault();
            $scope.submit();
        };

        $scope.submit = function() {
            if($scope.inviteForm.$valid && $scope.registeredUsers) {
                var invitations = buildInvitationsFromEmails();
                sendInvitation(invitations, $scope.invitationLink).then(sendingSuccess, sendingError);
                $scope.emails = '';
                $scope.inviteForm.emails.$setPristine();
                Array.prototype.unshift.apply($scope.registeredUsers, invitations);
            }

            function buildInvitationsFromEmails() {
                return $scope.emails.split(/[\s;,]+/).map(function (email) {
                    return {email: email, pending: true};
                });
            }

            function sendingSuccess() {
                invitations.forEach(function(invitation) {
                    delete invitation.pending;
                    invitation.sendingOk = true;
                });
            }

            function sendingError() {
                invitations.forEach(function(invitation) {
                    delete invitation.pending;
                    invitation.sendingFailed = true;
                });
            }
        };

        function loadRegisteredUsers() {
            invitationService.loadRegisteredUsers().then(function(users) {
                $scope.registeredUsersLoaded = true;
                $scope.registeredUsers = users;
            });
        }

        function loadInvitationLink() {
            $scope.invitationLink = 'Generating invitation link...';
            invitationService.loadInvitationLink().then(function(result) {
                $scope.invitationLink = result;
            });
        }

        function sendInvitation(emails, invitationLink) {
            return invitationService.sendInvitation(emails, invitationLink);
        }

    });
