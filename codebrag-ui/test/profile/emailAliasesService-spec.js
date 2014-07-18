describe('EmailAliasesService', function() {

    var $rootScope, $httpBackend, emailAliasesService,
        authServiceStub = {
            loggedInUser: {
                id: 123
            }
        };

    beforeEach(module('codebrag.profile', function($provide) {
        $provide.value('authService', authServiceStub);
    }));

    beforeEach(inject(function (_$rootScope_, _$httpBackend_, _emailAliasesService_) {
        $rootScope = _$rootScope_;
        $httpBackend = _$httpBackend_;
        emailAliasesService = _emailAliasesService_;
    }));

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('should have empty list of aliases for user', function() {
        expect(emailAliasesService.aliases.length).toBe(0);
    });

    it('should load user aliases', function() {
        // given
        var aliases = [
            { id: 1, userId: 123, alias: 'email@codebrag.com'},
            { id: 2, userId: 456, alias: 'alias@codebrag.com'}
        ];
        $httpBackend.whenGET('rest/users/123/aliases').respond(aliases);

        // when
        emailAliasesService.loadAliases();
        $httpBackend.flush();

        // then
        expect(emailAliasesService.aliases.length).toBe(2);
    });

    describe('creating alias', function() {

        it('should create user alias', function() {
            // given
            var aliasToCreate = { userId: 123, email: 'email@codebrag.com'},
                aliasCreated = angular.copy(aliasToCreate, {id: 999});
            $httpBackend.expectPOST('rest/users/123/aliases', aliasToCreate).respond(aliasCreated);

            // when
            emailAliasesService.createAlias(aliasToCreate.email);
            $httpBackend.flush();

            // then
            expect(emailAliasesService.aliases.indexOf(aliasCreated) > -1).toBeTruthy();
        });

        it('should return errors when failed', function() {
            // given
            var aliasToCreate = { userId: 123, email: 'email@codebrag.com'},
                errors = [ { alias: ['Could not create alias'] } ];
            $httpBackend.expectPOST('rest/users/123/aliases', aliasToCreate).respond(400, errors);

            // when
            var errorsReceived = [];
            emailAliasesService.createAlias(aliasToCreate.email).then(null, function(e) {
                errorsReceived = e;
            });
            $httpBackend.flush();

            // then
            expect(errorsReceived).toEqual(errors);
        });

    });

    describe('removing alias', function() {

        it('should remove user alias', function() {
            // given
            var aliasToRemove = { id: 999, userId: 123, email: 'email@codebrag.com'};
            emailAliasesService.aliases.push(aliasToRemove);
            $httpBackend.expectDELETE('rest/users/123/aliases/999').respond(200);

            // when
            emailAliasesService.deleteAlias(aliasToRemove);
            $httpBackend.flush();

            // then
            expect(emailAliasesService.aliases.length).toBe(0);
        });

        it('should return errors when failed', function() {
            // given
            var aliasToRemove = { id: 999, userId: 123, email: 'email@codebrag.com'},
                errors = [ { alias: ['Could not delete alias'] } ];
            emailAliasesService.aliases.push(aliasToRemove);
            $httpBackend.expectDELETE('rest/users/123/aliases/999').respond(400, errors);

            // when
            var errorsReceived = [];
            emailAliasesService.deleteAlias(aliasToRemove).then(null, function(e) {
                errorsReceived = e;
            });
            $httpBackend.flush();

            // then
            expect(errorsReceived).toEqual(errors);
            expect(emailAliasesService.aliases.length).toBe(1);
        });

    });
});