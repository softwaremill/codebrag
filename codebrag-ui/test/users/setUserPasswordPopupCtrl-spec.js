 'use strict';

describe("SetUserPasswordPopupCtrl", function () {

    var scope,
        $rootScope,
        $q,
        $controller,
        userMgmtService;

    beforeEach(module('codebrag.userMgmt'));

    beforeEach(inject(function(_$rootScope_, _$q_, _$controller_, _userMgmtService_) {
        $rootScope = _$rootScope_;
        $q = _$q_;
        $controller = _$controller_;
        userMgmtService = _userMgmtService_;

        scope = $rootScope.$new();
    }));

    it('should change user password and close modal', function () {
        // Given
        var targetUser = { userId: 100, name: 'John Doe' };
        var changedUserData = { userId: targetUser.userId, newPass: 'secret' };
        spyOn(userMgmtService, 'modifyUser').andReturn($q.when());
        var modalInstance = jasmine.createSpyObj('modal', ['close']);
        $controller('SetUserPasswordPopupCtrl', {$scope: scope, $modalInstance: modalInstance, user: targetUser});

        // When
        scope.setUserPassword(changedUserData);
        scope.$digest();

        // Then
        expect(userMgmtService.modifyUser).toHaveBeenCalledWith(changedUserData);
        expect(modalInstance.close).toHaveBeenCalled();
    });

    it('should display error and not close modal when password could not be changed', function () {
        // Given
        var targetUser = { userId: 100, name: 'John Doe' };
        var changedUserData = { userId: targetUser.userId, newPass: 'secret' };
        spyOn(userMgmtService, 'modifyUser').andReturn($q.reject());
        var modalInstance = jasmine.createSpyObj('modal', ['close']);
        $controller('SetUserPasswordPopupCtrl', {$scope: scope, $modalInstance: modalInstance, user: targetUser});

        // When
        scope.setUserPassword(changedUserData);
        scope.$digest();

        // Then
        expect(userMgmtService.modifyUser).toHaveBeenCalledWith(changedUserData);
        expect(modalInstance.close).not.toHaveBeenCalled();
        expect(scope.flash.get('error')[0].message).toBe('Could not set user password');
    });
});
