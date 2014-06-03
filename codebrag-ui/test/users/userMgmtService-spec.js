 'use strict';

describe("UserMgmtService", function () {

    var $rootScope,
        $q,
        $modal,
        $httpBackend,
        userMgmtService,
        registeredUsers = [
            { id: 100, name: 'John Doe', email: 'john@doe.com', admin: true, active: true },
            { id: 200, name: 'Mary Smith', email: 'mary@smith.com', admin: true, active: false }
        ];

    beforeEach(module('codebrag.userMgmt', 'ui.bootstrap.modal'));

    beforeEach(inject(function(_$rootScope_, _$q_, _userMgmtService_, _$modal_, _$httpBackend_) {
        $rootScope = _$rootScope_;
        $q = _$q_;
        $modal = _$modal_;
        $httpBackend = _$httpBackend_;
        userMgmtService = _userMgmtService_;
    }));

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('should initialize listener for event to open popup', function () {
        // Given
        spyOn($modal, 'open');

        // When
        userMgmtService.initialize();
        $rootScope.$broadcast('openUserMgmtPopup');

        // Then
        expect($modal.open).toHaveBeenCalled();
    });

    it('should load registerd users', function () {
        // Given
        $httpBackend.expectGET('rest/users/all').respond({users: registeredUsers});

        // When
        var loadedUsers = null;
        userMgmtService.loadUsers().then(function(users) {
            loadedUsers = users;
        });
        $httpBackend.flush();

        // Then
        expect(loadedUsers).toEqual(registeredUsers);
    });

    xit('should modify user data', function () {
        throw new Error('not yet implemented');
    });

});
