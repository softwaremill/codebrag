describe('General likes directive', function() {

    var scope, el, $httpBackend;

    var loggedInUser = {
        id: '123',
        fullName: 'Hulk Hogan'
    };

    var commit = {
        commit: {
            id: '999',
            authorName: 'Steven Segal'
        },
        reactions: {
            likes: []
        }
    };

    var like = {
        authorId: "123",
        authorName: "Hulk Hogan",
        id: "456",
        time: "2013-09-11T20:16:05Z"
    };

    beforeEach(module('codebrag.templates'));
    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function ($rootScope, authService, _$httpBackend_) {
        $httpBackend = _$httpBackend_;
        scope = $rootScope;
        authService.loggedInUser = loggedInUser;
        scope.currentCommit = new codebrag.CurrentCommit(commit);
        commit._toRestore = angular.copy(commit);
    }));

    beforeEach(inject(function ($compile) {
        el = angular.element('<div><general-likes commit="currentCommit"></general-likes></div>');
        $compile(el)(scope);
        scope.$digest();
    }));

    afterEach(function() {
        commit = commit._toRestore;
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('should display text that there are no likes for commit if user is commit author', function() {
        // given
        commit.commit.authorName = loggedInUser.fullName;

        // when
        scope.$digest();

        // then
        var text = el.find('span').text();
        expect(text).toBe('No likes for this commit yet');
    });

    it('should display text encouraging for first like if user is not commit author', function() {
        var text = el.find('span').text();
        expect(text).toBe('Be the first to like this commit');
    });

    it('should display text that user likes given commit', function() {
        // given
        scope.currentCommit.reactions.likes.push(like);

        // when
        scope.$digest();

        // then
        var text = el.find('span').text();
        expect(text).toBe('Hulk Hogan likes this commit');
    });

    it('should display correct form when more than one user likes commit', function() {
        // given
        var anotherLike = {
            authorId: "516d05d1e4b0ad2b39ba1b39",
            authorName: "Steven Segal",
            id: "5230cf85e4b031e73929a32e",
            time: "2013-09-11T20:16:05Z"
        };
        scope.currentCommit.reactions.likes.push(like);
        scope.currentCommit.reactions.likes.push(anotherLike);

        // when
        scope.$digest();

        // then
        var text = el.find('span').text();
        expect(text).toMatch(/.* like this commit/);
    });

    it('should send like request to server and display that user likes commit', function() {
        // given
        $httpBackend.expectPOST('rest/commits/999/likes', {commitId: commit.commit.id}).respond(like);

        // when
        el.find('a').click();
        $httpBackend.flush();

        // then
        var text = el.find('span').text();
        expect(text).toBe('Hulk Hogan likes this commit');
    });

    it('should unlike if user already likes that commit', function() {
        // given
        scope.currentCommit.reactions.likes.push(like);
        $httpBackend.expectDELETE('rest/commits/999/likes/' + like.id).respond(200);

        // when
        el.find('a').click();
        $httpBackend.flush();

        // then
        var text = el.find('span').text();
        expect(text).toBe('Be the first to like this commit');
    });

    it('should have additional css class when commit is already liked by user', function() {
        // given
        var div = el.find('div');
        expect(div).not.toHaveClass('liked-by-user');
        scope.currentCommit.reactions.likes.push(like);

        // when
        scope.$digest();

        // then
        expect(div).toHaveClass('liked-by-user');
    });

    it('should trigger like action when user clicks on text too', function() {
        // given
        var textEl = el.find('span');
        $httpBackend.expectPOST('rest/commits/999/likes', {commitId: commit.commit.id}).respond(like);

        // when
        textEl.click();
        $httpBackend.flush();

        // then
        expect(textEl.text()).toBe('Hulk Hogan likes this commit');
    });

});