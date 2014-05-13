describe("User settings service", function () {

    var $rootScope;
    var userSettingsService, events;

    beforeEach(module('codebrag.profile'));

    beforeEach(inject(function (_userSettingsService_, _$rootScope_, _events_) {
        $rootScope = _$rootScope_;
        userSettingsService = _userSettingsService_;
        events = _events_;
    }));

    it('should store user-selected branch on branch change event', function() {
        // given
        var selectedBranchName = 'bugfix';
        spyOn(userSettingsService, 'save');

        // when
        $rootScope.$broadcast(events.branches.branchChanged, selectedBranchName);

        // then
        expect(userSettingsService.save).toHaveBeenCalledWith({selectedBranch: selectedBranchName});
    });

});
