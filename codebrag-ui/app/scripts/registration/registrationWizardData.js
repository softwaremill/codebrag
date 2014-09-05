angular.module('codebrag.registration')

    /*
    Keeps context information related to user registration,
    like invitation code and user Id when first step was already done.
    Also decides which registration step should be visible at given time
     */

    .factory('registrationWizardData', function() {

        return {
            invitationCode: null,
            registeredUser: null,
            signupVisible: function() {
                return Boolean(this.invitationCode && !this.registeredUser)
            },
            watchVisible: function() {
                return Boolean(this.invitationCode && this.registeredUser);
            }
        };

    });

