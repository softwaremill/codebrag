angular.module('codebrag.registration')

    /*
    Entry controler for registration, takes invitationId as parameter from location
    and updates registration data.
     */
    .controller('RegistrationWizardCtrl', function($scope, registrationWizardData, invitationId) {

        $scope.wizard = registrationWizardData;
        registrationWizardData.invitationCode = invitationId;

    });

