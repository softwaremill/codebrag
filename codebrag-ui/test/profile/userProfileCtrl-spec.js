describe('UserProfileCtrl', function() {

    var $rootScope, $controller, $q;
    var authService, configService, userSettingsService;
    var scope;

    var currentUser = {
        login: 'john',
        email: 'john@codebrag.com'
    };

    beforeEach(module('codebrag.profile'));

    beforeEach(inject(function (_$rootScope_, _$q_, _$controller_, _authService_, _configService_, _userSettingsService_) {
        $rootScope = _$rootScope_;
        $q = _$q_;
        $controller = _$controller_;
        userSettingsService = _userSettingsService_;
        configService = _configService_;
        authService = _authService_;

        scope = $rootScope.$new();
    }));

    it('load user info on start to display profile', function() {
        spyOn(authService, 'requestCurrentUser').andReturn($q.when(currentUser));
        spyOn(configService, 'fetchConfig').andReturn($q.when({}));

        $controller('UserProfileCtrl', {$scope: scope});
        $rootScope.$apply();

        expect(scope.user).toEqual(currentUser);
    });

    it('load user notification settings only when global notifications are enabled', function() {
        var appConfig = {
            emailNotifications: true
        };
        var dummyUserSettings = 'user_settings';
        spyOn(authService, 'requestCurrentUser').andReturn($q.when(currentUser));
        spyOn(configService, 'fetchConfig').andReturn($q.when(appConfig));
        spyOn(userSettingsService, 'load').andReturn($q.when(dummyUserSettings));

        $controller('UserProfileCtrl', {$scope: scope});
        $rootScope.$apply();

        expect(scope.userSettings).toEqual(dummyUserSettings);
    });

    it('load not load user notification settings when global notifications are disabled', function() {
        var appConfig = {
            emailNotifications: false
        };
        spyOn(authService, 'requestCurrentUser').andReturn($q.when(currentUser));
        spyOn(configService, 'fetchConfig').andReturn($q.when(appConfig));
        spyOn(userSettingsService, 'load');

        $controller('UserProfileCtrl', {$scope: scope});
        $rootScope.$apply();

        expect(userSettingsService.load).not.toHaveBeenCalled();
        expect(scope.userSettings).toBeUndefined();
    });

    describe('Change operation status when saving', function() {

        beforeEach(function() {
            var appConfig = {
                emailNotifications: true
            };
            var dummyUserSettings = 'user_settings';
            spyOn(authService, 'requestCurrentUser').andReturn($q.when(currentUser));
            spyOn(configService, 'fetchConfig').andReturn($q.when(appConfig));
            spyOn(userSettingsService, 'load').andReturn($q.when(dummyUserSettings));

            $controller('UserProfileCtrl', {$scope: scope});
            $rootScope.$apply();
        });

        it('should set pending status when saving changes', function() {

            scope.notificationsChanged();

            expect(scope.savingStatus).toBe('pending');
        });

        it('should set status to ok when changes saved', function() {
            spyOn(userSettingsService, 'save').andReturn($q.when());

            scope.notificationsChanged();
            $rootScope.$apply();

            expect(scope.savingStatus).toBe('success');
        });

        it('should set status to error when changes not saved', function() {
            spyOn(userSettingsService, 'save').andReturn($q.reject());

            scope.notificationsChanged();
            $rootScope.$apply();

            expect(scope.savingStatus).toBe('failed');
        });

    });

});