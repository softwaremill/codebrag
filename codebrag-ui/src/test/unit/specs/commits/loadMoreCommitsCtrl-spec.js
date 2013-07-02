'use strict';

describe("LoadMoreCommitsCtrl", function () {

    var rootScope, createCtrl, q;

    beforeEach(module('codebrag.commits'));
    beforeEach(module('codebrag.events'));

    beforeEach(inject(function ($rootScope, $controller, $q) {
        rootScope = $rootScope;
        createCtrl = $controller;
        q = $q;
    }));

    it('should thrown an error when no commits available in scope', function() {
        // when
        var controllerInit = function() { createCtrl('LoadMoreCommitsCtrl', {$scope: {}}) };

        // then
        expect(controllerInit).toThrow('Commits list promise not available')
    });

    it('should enable loading more when there is more commits than loaded', inject(function($rootScope, events) {
        // given
        var expectedValue;
        var scope = $rootScope.$new();
        scope.commits = ['commit one'];
        createCtrl('LoadMoreCommitsCtrl', {$scope: scope});

        // when
        $rootScope.$broadcast(events.commitCountChanged, {commitCount: 5});
        scope.moreCommitsAvailable.then(function(value) {
            expectedValue = value;
        });
        scope.$apply();

        // then
        expect(expectedValue).toBe(true);
    }));

    it('should disable loading more when there is no more commits to load', inject(function($rootScope, events) {
        // given
        var expectedValue;
        var scope = $rootScope.$new();
        scope.commits = ['commit one', 'commit two'];
        createCtrl('LoadMoreCommitsCtrl', {$scope: scope});

        // when
        $rootScope.$broadcast(events.commitCountChanged, {commitCount: 2});
        scope.moreCommitsAvailable.then(function(value) {
            expectedValue = value;
        });
        scope.$apply();

        // then
        expect(expectedValue).toBe(false);
    }));

});
