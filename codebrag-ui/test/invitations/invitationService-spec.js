describe("Invitation service", function () {

    var $httpBackend;
    var invitationService;
    
    beforeEach(module('codebrag.invitations'));

    beforeEach(inject(function (_$httpBackend_, _invitationService_) {
        $httpBackend = _$httpBackend_;
        invitationService = _invitationService_;
    }));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

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
