describe('General likes directive', function() {

    var scope, el, $httpBackend, $compile;

    var labelSelector = '#likesLabel';
    var likingUsersSelector = '.user-who-like';

    var loggedInUser = {
        id: '123',
        fullName: 'Hulk Hogan',
        email: 'hulk@hogan.com'
    };

    var anotherUser = {
        id: '456',
        fullName: 'Steven Segal',
        email: 'steven@segal.com'
    };

    var emptyCommit = {
        commit: {
            id: '999'
        },
        reactions: {
            likes: []
        }
    };

    var like = {
        authorId: loggedInUser.id,
        authorName: loggedInUser.fullName,
        id: "666",
        time: "2013-09-11T20:16:05Z"
    };

    beforeEach(module('codebrag.templates'));
    beforeEach(module('codebrag.commits'));

    beforeEach(inject(function ($rootScope, authService, _$httpBackend_, _$compile_) {
        $httpBackend = _$httpBackend_;
        $compile = _$compile_;
        scope = $rootScope;
        authService.loggedInUser = loggedInUser;
    }));

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    describe('when current user is commit author', function () {

        var commit;

        beforeEach(function () {
            commit = angular.copy(emptyCommit);
            commit.commit.authorName = loggedInUser.fullName;
            commit.commit.authorEmail = loggedInUser.email;
            scope.currentCommit = new codebrag.CurrentCommit(commit);
            el = angular.element('<div><general-likes commit="currentCommit"></general-likes></div>');
            $compile(el)(scope);
            scope.$digest();
        });

        it('should display text that there are no likes for commit', function() {
            // given
            expect(labelText(el)).toBe('No likes for this commit yet');
        });

    });

    describe('when user is not commit author', function() {

        var commit;

        beforeEach(inject(function ($compile) {
            commit = angular.copy(emptyCommit);
            commit.commit.authorName = anotherUser.fullName;
            commit.commit.authorEmail = anotherUser.email;
            scope.currentCommit = new codebrag.CurrentCommit(commit);
            el = angular.element('<div><general-likes commit="currentCommit"></general-likes></div>');
            $compile(el)(scope);
            scope.$digest();
        }));

        it('should display text encouraging for first like', function() {
            expect(labelText(el)).toBe('Be the first to like this commit');
        });

        it('should display text that user likes given commit', function() {
            // given
            scope.currentCommit.reactions.likes.push(like);

            // when
            scope.$digest();

            // then
            userNames(el);
            expect(userNames(el)).toEqual(['Hulk Hogan']);
            expect(labelText(el)).toBe('likes this commit');
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
            expect(labelText(el)).toBe('like this commit');
        });

        it('should send like request to server and display that user likes commit', function() {
            // given
            $httpBackend.expectPOST('rest/commits/999/likes', {commitId: emptyCommit.commit.id}).respond(like);

            // when
            el.find('a').click();
            $httpBackend.flush();

            // then
            expect(userNames(el)).toEqual(['Hulk Hogan']);
        });

        it('should unlike if user already likes that commit', function() {
            // given
            scope.currentCommit.reactions.likes.push(like);
            $httpBackend.expectDELETE('rest/commits/999/likes/' + like.id).respond(200);

            // when
            el.find('a').click();
            $httpBackend.flush();

            // then
            expect(labelText(el)).toBe('Be the first to like this commit');
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
            var textEl = el.find(labelSelector);
            $httpBackend.expectPOST('rest/commits/999/likes', {commitId: emptyCommit.commit.id}).respond(like);

            // when
            textEl.click();
            $httpBackend.flush();

            // then
            var newLength = scope.currentCommit.reactions.likes.length;
            expect(newLength).toBe(1);
        });

    });

    function labelText(el) {
        return el.find(labelSelector).text();
    }

    function userNames(el) {
        var users = [];
        el.find(likingUsersSelector).each(function() {
            users.push($(this).text());
        });
        return users;
    }
});