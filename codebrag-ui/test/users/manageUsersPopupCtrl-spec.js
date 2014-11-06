 'use strict';

describe("ManageUsersPopupCtrl", function () {

    var scope,
        $rootScope,
        $q,
        $controller,
        userMgmtService,
        popupsService,
        registeredUsers = [
            { userId: 100, name: 'John Doe', email: 'john@doe.com', admin: true, active: true },
            { userId: 200, name: 'Mary Smith', email: 'mary@smith.com', admin: true, active: false }
        ];

    beforeEach(module('codebrag.userMgmt', 'codebrag.common.services'));

    beforeEach(inject(function(_$rootScope_, _$q_, _$controller_, _userMgmtService_, _popupsService_) {
        $rootScope = _$rootScope_;
        $q = _$q_;
        $controller = _$controller_;
        userMgmtService = _userMgmtService_;
        popupsService = _popupsService_;

        scope = $rootScope.$new();
    }));

    it('should load registered users list on start', function () {
        // Given
        var registerdUsersPromise = $q.when(registeredUsers);
        spyOn(userMgmtService, 'loadUsers').andReturn(registerdUsersPromise);

        // When
        $controller('ManageUsersPopupCtrl', {$scope: scope});
        scope.$digest();

        // Then
        expect(scope.users).toEqual(registeredUsers);
    });

    describe('with initial data loaded', function() {

        beforeEach(function() {
            spyOn(userMgmtService, 'loadUsers').andReturn($q.when(registeredUsers));
            $controller('ManageUsersPopupCtrl', {$scope: scope});
            scope.$digest();
        });

        it('should count active users', function () {
            // When
            var activeUsers = scope.countActiveUsers();

            // Then
            expect(activeUsers).toBe(1);
        });

        it('should open invite popup', function() {
            // Given
            spyOn(popupsService, 'openInvitePopup');

            // When
            scope.invite();

            // Then
            expect(popupsService.openInvitePopup).toHaveBeenCalled();

        });

        it('should open popup to set user password and display info when done', function() {
            // Given
            var popup = { result: $q.when() };
            spyOn(popupsService, 'openSetUserPasswordPopup').andReturn(popup);
            var user = registeredUsers[0];

            // When
            scope.askForNewPassword(user);
            scope.$digest();

            // Then
            var expectedMsg = { type: 'info', message: 'User password changed' };
            expect(popupsService.openSetUserPasswordPopup).toHaveBeenCalledWith(user);
            expect(scope.flash.get('info')[0]).toEqual(expectedMsg);
        });

        it('should lock user row while saving changes', function() {
            // Given
            var user = registeredUsers[0];
            spyOn(userMgmtService, 'modifyUser').andReturn($q.when());

            // When
            scope.modifyUser(user);
            expect(user.locked).toEqual(true);

            // Then
            scope.$digest();
            expect(user.locked).toBeUndefined();
        });

        it('should change only property provided', function() {
            // Given
            var user = registeredUsers[0];
            spyOn(userMgmtService, 'modifyUser').andReturn($q.when());

            // When
            scope.modifyUser(user, 'active');

            // Then
            expect(userMgmtService.modifyUser).toHaveBeenCalledWith({userId: user.userId, active: user.active });
        });

        it('should change property back when saving changes failed', function() {
            // Given
            var user = registeredUsers[0];
            spyOn(userMgmtService, 'modifyUser').andReturn($q.reject({}));

            // When
            user.active = true;
            scope.modifyUser(user, 'active');
            scope.$digest();

            // Then
            expect(user.active).toEqual(false);
        });

        it('should display info when changes saved', function() {
            // Given
            var user = registeredUsers[0];
            spyOn(userMgmtService, 'modifyUser').andReturn($q.when());

            // When
            scope.modifyUser(user);
            scope.$digest();

            // Then
            var expectedMsg = 'User details changed';
            expect(getFlashMessage('info')).toEqual(expectedMsg);
        });

        it('should display errors when changes not saved', function() {
            // Given
            var user = registeredUsers[0];
            var serverErrors = { dummy: "server error message" };
            spyOn(userMgmtService, 'modifyUser').andReturn($q.reject(serverErrors));

            // When
            scope.modifyUser(user);
            scope.$digest();

            // Then
            var expectedErrors = ['Could not change user details', 'server error message'];
            var actualErrors = scope.flash.get('error').map(function(e) { return e.message; } );
            expect(actualErrors).toEqual(expectedErrors);
        });

        function getFlashMessage(type){
            return scope.flash.get(type)[0].message;
        }
    });


});
