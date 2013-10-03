'use strict';

describe("Invite form Controller", function () {

    var $rootScope, $controller, $q;
    var invitationService, scope;

    var registeredUsers = ['user1', 'user2'];
    var invitationLink = 'http://codebrag.com/register/abc';

    beforeEach(module('codebrag.invitations'));

    beforeEach(inject(function (_$rootScope_, _invitationService_, _$controller_, _$q_) {
        $rootScope = _$rootScope_;
        $controller = _$controller_;
        $q = _$q_;
        invitationService = _invitationService_;
        scope = $rootScope.$new();

        spyOn(invitationService, 'loadRegisteredUsers').andReturn($q.when(registeredUsers));
        spyOn(invitationService, 'loadInvitationLink').andReturn($q.when(invitationLink));
    }));

    it('should load already registered users and registration link on start', function() {
        // given

        // when
        $controller('InviteFormPopupCtrl', {$scope: scope, invitationService: invitationService});
        scope.$apply();

        // then
        expect(scope.registeredUsers).toBe(registeredUsers);
        expect(scope.invitationLink).toBe(invitationLink);
    });

    it('should not send invitation when emails provided are not valid', function() {
        // given
        spyOn(invitationService, 'sendInvitation');
        scope.inviteForm = {$valid: false};
        $controller('InviteFormPopupCtrl', {$scope: scope, invitationService: invitationService});
        scope.$apply();

        // when
        scope.submit();

        // then
        expect(invitationService.sendInvitation).not.toHaveBeenCalled();
    });

    describe('Sending invitation', function() {

        var newEmail = 'john@codebrag.com';

        beforeEach(function() {
            $controller('InviteFormPopupCtrl', {$scope: scope, invitationService: invitationService});
            scope.$apply();
            scope.inviteForm = {
                $valid: true,
                emails: {
                    $valid: true,
                    $setPristine: angular.noop
                }
            };
            spyOn(scope.inviteForm.emails, '$setPristine');
            scope.emails = newEmail;
        });

        it('should send invitation with generated link and emails provided', function() {
            // given
            spyOn(invitationService, 'sendInvitation').andReturn($q.when());

            // when
            scope.submit();

            // then
            var invitations = [{email: newEmail, pending: true}];
            expect(invitationService.sendInvitation).toHaveBeenCalledWith(invitations, invitationLink);
        });

        it('should add email to list with status pending when invitation sent', function() {
            // given
            spyOn(invitationService, 'sendInvitation').andReturn($q.when());

            // when
            scope.submit();

            // then
            var addedEmail = scope.registeredUsers.filter(function(user) {
                return user.email === newEmail;
            })[0];
            expect(addedEmail).toEqual({email: newEmail, pending: true});
        });

        it('should set status on emails when successfull sending response comes back', function() {
            // when
            spyOn(invitationService, 'sendInvitation').andReturn($q.when());
            scope.submit();
            scope.$apply();

            // then
            var addedEmail = scope.registeredUsers.filter(function(user) {
                return user.email === newEmail;
            })[0];
            expect(addedEmail).toEqual({email: newEmail, sendingOk: true});
        });

        it('should set status on emails when faield sending response comes back', function() {
            // when
            spyOn(invitationService, 'sendInvitation').andReturn($q.reject());
            scope.submit();
            scope.$apply();

            // then
            var addedEmail = scope.registeredUsers.filter(function(user) {
                return user.email === newEmail;
            })[0];
            expect(addedEmail).toEqual({email: newEmail, sendingFailed: true});
        });
    });



});
