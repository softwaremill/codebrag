describe('UserSettingsCtrl', function() {

    var $rootScope, $controller, $q, scope,
        configServiceStub, userSettingsServiceStub;


    beforeEach(module('codebrag.profile'));

    beforeEach(inject(function (_$rootScope_, _$q_, _$controller_) {
        $rootScope = _$rootScope_;
        $q = _$q_;
        $controller = _$controller_;

        scope = $rootScope.$new();
    }));

    describe('Loading notification settings', function() {

        beforeEach(function() {
            configServiceStub = jasmine.createSpyObj('configService', ['fetchConfig']);
            userSettingsServiceStub = jasmine.createSpyObj('userSettingsService', ['load']);
        });

        it('load user notification settings only when global notifications are enabled', function() {
            var appConfig = { emailNotifications: true };
            var dummyUserSettings = 'user_settings';
            configServiceStub.fetchConfig.andReturn($q.when(appConfig));
            userSettingsServiceStub.load.andReturn(dummyUserSettings);

            $controller('UserSettingsCtrl', {$scope: scope, userSettingsService: userSettingsServiceStub, configService: configServiceStub});
            $rootScope.$apply();

            expect(scope.userSettings).toEqual(dummyUserSettings);
        });

        it('load not load user notification settings when global notifications are disabled', function() {
            var appConfig = { emailNotifications: false };
            configServiceStub.fetchConfig.andReturn($q.when(appConfig));

            $controller('UserSettingsCtrl', {$scope: scope, userSettingsService: userSettingsServiceStub, configService: configServiceStub});
            $rootScope.$apply();

            expect(userSettingsServiceStub.load).not.toHaveBeenCalled();
            expect(scope.userSettings).toBeUndefined();
        });

    });

    describe('Block form when saving notifications', function() {

        var configServiceStub, userSettingsServiceStub;

        beforeEach(function() {
            configServiceStub = jasmine.createSpyObj('configService', ['fetchConfig']);
            userSettingsServiceStub = jasmine.createSpyObj('userSettingsService', ['load', 'save']);
            var appConfig = { emailNotifications: true };
            configServiceStub.fetchConfig.andReturn('user_settings');
            configServiceStub.fetchConfig.andReturn($q.when(appConfig));
        });

        it('should set actionPending flag when operation started', function() {
            userSettingsServiceStub.save.andReturn($q.when());

            $controller('UserSettingsCtrl', {$scope: scope, userSettingsService: userSettingsServiceStub, configService: configServiceStub});
            scope.notificationsChanged();

            expect(scope.actionPending).toBe(true);
        });

        it('should set actionPending flag to false when operation finished', function() {
            userSettingsServiceStub.save.andReturn($q.reject());

            $controller('UserSettingsCtrl', {$scope: scope, userSettingsService: userSettingsServiceStub, configService: configServiceStub});
            scope.notificationsChanged();
            scope.$apply();

            expect(scope.actionPending).toBe(false);
        });

    });

});