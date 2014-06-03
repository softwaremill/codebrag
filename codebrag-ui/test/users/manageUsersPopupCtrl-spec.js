 'use strict';

describe("ManageUsersPopupCtrl", function () {

    var scope,
        $rootScope,
        $q,
        $controller,
        userMgmtService,
        licenceService,
        popupsService,
        registeredUsers = [
            { id: 100, name: 'John Doe', email: 'john@doe.com', admin: true, active: true },
            { id: 200, name: 'Mary Smith', email: 'mary@smith.com', admin: true, active: false }
        ];

    beforeEach(module('codebrag.userMgmt', 'codebrag.licence', 'codebrag.common.services'));

    beforeEach(inject(function(_$rootScope_, _$q_, _$controller_, _userMgmtService_, _licenceService_, _popupsService_) {
        $rootScope = _$rootScope_;
        $q = _$q_;
        $controller = _$controller_;
        userMgmtService = _userMgmtService_;
        licenceService = _licenceService_;
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

    it('should load max allowed users from licence data', function () {
        // Given
        var licenceData = { maxUsers: 50 };
        spyOn(userMgmtService, 'loadUsers').andReturn($q.when([]));
        spyOn(licenceService, 'ready').andReturn($q.when(licenceData));

        // When
        $controller('ManageUsersPopupCtrl', {$scope: scope});
        scope.$digest();

        // Then
        expect(scope.licenceMaxUsers).toEqual(licenceData.maxUsers);
    });

    describe('with initial data loaded', function() {

        beforeEach(function() {
            spyOn(userMgmtService, 'loadUsers').andReturn($q.when(registeredUsers));
            spyOn(licenceService, 'ready').andReturn($q.when({}));
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

        it('should display error when changes not saved', function() {
            // Given
            var user = registeredUsers[0];
            spyOn(userMgmtService, 'modifyUser').andReturn($q.reject());

            // When
            scope.modifyUser(user);
            scope.$digest();

            // Then
            var expectedMsg = 'Could not change user details';
            expect(getFlashMessage('error')).toEqual(expectedMsg);
        });

        function getFlashMessage(type){
            return scope.flash.get(type)[0].message;
        }
    });


});
