describe('Saving status indicator directive', function() {

    var $rootScope, $scope, el, $compile;
    var statusClasses = ['pending', 'success', 'failed'];

    beforeEach(module('codebrag.common.directives'));

    beforeEach(inject(function (_$rootScope_, _$compile_) {
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        $scope = $rootScope.$new();

        el = angular.element('<span saving-status="status"></span>');
        $compile(el)($scope);
    }));

    it('should be empty with no status-related class on start', function() {
        expect(el.text()).toBe('');
        statusClasses.forEach(function(clazz) {
            expect(el.hasClass(clazz)).toBeFalsy();
        })
    });

    it('should show pending with class pending when saving in progress', function() {
        $scope.status = 'pending';
        $scope.$apply();
        expect(el.text()).toBe('Saving...');
        expect(el.hasClass($scope.status)).toBeTruthy();
    });

    it('should show success with class success when saving was ok', function() {
        $scope.status = 'success';
        $scope.$apply();
        expect(el.text()).toBe('Saved');
        expect(el.hasClass($scope.status)).toBeTruthy();
    });
});