describe("Current Commit", function () {

    var oneUser = {id: '111', name: 'Current User', email: 'current@email.com'};
    var otherUser = {id: '222', name: 'Other User', email: 'other@email.com'};
    var commitAuthor = {id: '333', name: 'Commit Author', email: 'commit@author.com'};

    var emptyCommit;

    beforeEach(function() {
        emptyCommit = {
            commit: {
                authorName: commitAuthor.name,
                authorEmail: commitAuthor.email
            },
            reactions: {},
            lineReactions: [],
            diff: {},
            suppressedFiles: {}
        };
    });

    it('should add some likes to a line', function () {
        // Given
        var commit = new codebrag.CurrentCommit(emptyCommit);
        var like1 = createLike(oneUser);
        var like2 = createLike(otherUser);
        var fileName = 'filename.txt';
        var lineNumber = 17;

        // When
        commit.addLike(like1, fileName, lineNumber);
        commit.addLike(like2, fileName, lineNumber);

        // Then
        var likes = commit.lineReactions[fileName][lineNumber]['likes'];
        expect(likes.length).toBe(2);
        expect(likes[0]).toBe(like1);
        expect(likes[1]).toBe(like2);
    });

    it('should add some likes to different files and lines', function () {
        // Given
        var commit = new codebrag.CurrentCommit(emptyCommit);
        var like1 = createLike(oneUser, 'file.js', 10);
        var like2 = createLike(oneUser, 'file.scala', 20);

        // When
        commit.addLike(like1, like1.fileName, like1.lineNumber);
        commit.addLike(like2, like2.fileName, like2.lineNumber);

        // Then
        var likes1 = commit.lineReactions[like1.fileName][like1.lineNumber]['likes'];
        expect(likes1.length).toBe(1);
        expect(likes1[0]).toBe(like1);

        var likes2 = commit.lineReactions[like2.fileName][like2.lineNumber]['likes'];
        expect(likes2.length).toBe(1);
        expect(likes2[0]).toBe(like2);
    });

    it('should correctly recognize author of commit', function() {
        // Given
        var commit = new codebrag.CurrentCommit(emptyCommit);

        // Then
        expect(commit.isUserAuthorOfCommit(commitAuthor)).toBeTruthy();
        expect(commit.isUserAuthorOfCommit(otherUser)).toBeFalsy();
    });

    it('should correctly determine whether a user already liked a line or not', function() {
        // Given
        var commit = new codebrag.CurrentCommit(emptyCommit);
        var like = createLike(oneUser, 'test.txt', 444);
        commit.addLike(like, like.fileName, like.lineNumber);

        // Then
        expect(commit.userAlreadyLikedLine(oneUser, like.fileName, like.lineNumber)).toBeTruthy();
        expect(commit.userAlreadyLikedLine(otherUser, like.fileName, like.lineNumber)).toBeFalsy();
    });

    it('should add general likes to a commit', function() {
        // Given
        var commit = new codebrag.CurrentCommit(emptyCommit);
        var like1 = createLike(oneUser);
        var like2 = createLike(otherUser);
        commit.addGeneralLike(like1);
        commit.addGeneralLike(like2);

        // Then
        var likes = commit.reactions["likes"];
        expect(likes.length).toBe(2);
        expect(likes[0]).toEqual(like1);
        expect(likes[1]).toEqual(like2);
    });

    it('should correctly determine whether a user already liked entire commit or not', function() {
        // Given
        var commit = new codebrag.CurrentCommit(emptyCommit);
        var like = createLike(oneUser);
        commit.addGeneralLike(like);

        // Then
        expect(commit.userAlreadyLikedCommit(oneUser)).toBeTruthy();
        expect(commit.userAlreadyLikedCommit(otherUser)).toBeFalsy();
    });

    it('should say that user did not like a commit if there are no likes for commit', function() {
        // Given
        var commit = new codebrag.CurrentCommit(emptyCommit);

        // Then
        expect(commit.userAlreadyLikedCommit(oneUser)).toBeFalsy();
    });

    function createLike(user, fileName, lineNumber) {
        var like = {
            authorId: user ? user.id : '123',
            authorName: user ? user.name : 'User Name'
        };
        fileName && angular.extend(like, {fileName: fileName});
        lineNumber && angular.extend(like, {lineNumber: lineNumber});
        return like;
    }
});
