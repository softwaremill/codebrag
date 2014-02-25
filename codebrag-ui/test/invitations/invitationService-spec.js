describe("Invitation service", function () {

    var $httpBackend;
    var invitationService;
    var baseAppUrl;
    var $rootScope;
    var url = 'http://codebrag.com/#/';
    
    beforeEach(module('codebrag.invitations'));

    beforeEach(module(function($provide) {
        baseAppUrl = jasmine.createSpy('baseAppUrl').andReturn(url);
        $provide.value('baseAppUrl', baseAppUrl);
    }));

    beforeEach(inject(function (_$httpBackend_, _invitationService_, _$rootScope_) {
        $httpBackend = _$httpBackend_;
        invitationService = _invitationService_;
        $rootScope = _$rootScope_;
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    it('should build invitation link using reg code and browser url', function() {
        // given
        var codeResponse = {invitationCode: '123abc'};
        $httpBackend.whenGET('rest/invitation').respond(codeResponse);

        // when
        var linkReceived = null;
        invitationService.loadInvitationLink().then(function(l) {
            linkReceived = l;
        });

        // then
        $httpBackend.flush();
        expect(linkReceived).toBe('http://codebrag.com/#/register/123abc');
    });

    it('should send invitation with emails and invitation link', function () {
        // given
        var link = 'http://codebrag.com/register/123';
        var emails = ['john@codebrag.com', 'mary@codebrag.com'];
        var invitations = emails.map(function(e) {
            return {email: e, pending: true};
        });
        var expectedData = {
            emails: emails,
            invitationLink: link
        };
        $httpBackend.expectPOST('rest/invitation', expectedData).respond(200);

        // when
        invitationService.sendInvitation(invitations, link);
        $httpBackend.flush();

        // then correct http request expected
    });

    it('should successfully validate various valid emails', function() {
        var validEmails = [
            'john@codebrag.com',
            'john.doe@codebrag.com',
            'john@codebrag.com',
            'john_doe@code.brag.com',
            'john_doe@code.br',
            'john-doe@code.brag.co'
        ];

        validEmails.forEach(function(email) {
            expect(invitationService.validateEmails(email)).toBeTruthy();
        })
    });

    it('should fail on validating invalid emails', function() {
        var validEmails = [
            '@codebrag.com',
            'john.doe@',
            'john@codebrag',
            'john doe@code.brag.com'
        ];

        validEmails.forEach(function(email) {
            expect(invitationService.validateEmails(email)).toBeFalsy();
        })
    });
});

describe("Extracting base app url from browsers window", function() {

    var $location, baseAppUrl;

    beforeEach(module('codebrag.invitations'));

    beforeEach(inject(function(_$location_, _baseAppUrl_) {
        $location = _$location_;
        baseAppUrl = _baseAppUrl_;
    }));

    function test(fullUrl, path, expectedBaseAppUrl) {
        it('should extract base url from ' + fullUrl, function() {
            // given
            spyOn($location, 'absUrl').andReturn(fullUrl);
            spyOn($location, 'path').andReturn(path);

            // when
            var extracted = baseAppUrl();

            // then
            expect(extracted).toBe(expectedBaseAppUrl);
        });
    }

    var testCases = [
        {fullUrl: 'http://codebrag.com/#/', path: '/', expected: 'http://codebrag.com/#/'},
        {fullUrl: 'http://codebrag.com/#/commits', path: '/commits', expected: 'http://codebrag.com/#/'}
    ];

    testCases.forEach(function(data) {
        test(data.fullUrl, data.path, data.expected);
    })
});