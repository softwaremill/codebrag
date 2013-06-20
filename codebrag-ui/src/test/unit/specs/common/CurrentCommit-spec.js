describe("Current Commit", function () {

    var rootScope;

    beforeEach(module('codebrag.common'));

    beforeEach(inject(function ($rootScope) {
        rootScope = $rootScope;
    }));

    it('should add some likes to a line', inject(function () {
        // Given
        var emptyCommitData = _emptyCommitData();
        var like1 = _randomLike();
        var like2 = _randomLike();
        var commit = new codebrag.CurrentCommit(emptyCommitData);
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
    }));

    it('should add some likes to different files and lines', inject(function () {
        // Given
        var emptyCommitData = _emptyCommitData();
        var like1 = _randomLike();
        var like2 = _randomLike();
        var like3 = _randomLike();

        var commit = new codebrag.CurrentCommit(emptyCommitData);
        var fileName1 = 'filename.txt';
        var fileName2 = 'filename2.txt';

        var lineNumber1File1 = 17;
        var lineNumber2File1 = 54;
        var lineNumber1File2 = 500;

        // When
        commit.addLike(like1, fileName1, lineNumber1File1);
        commit.addLike(like2, fileName1, lineNumber2File1);
        commit.addLike(like3, fileName2, lineNumber1File2);

        // Then
        var likes1 = commit.lineReactions[fileName1][lineNumber1File1]['likes'];
        expect(likes1.length).toBe(1);
        expect(likes1[0]).toBe(like1);

        var likes2 = commit.lineReactions[fileName1][lineNumber2File1]['likes'];
        expect(likes2.length).toBe(1);
        expect(likes2[0]).toBe(like2);

        var likes3 = commit.lineReactions[fileName2][lineNumber1File2]['likes'];
        expect(likes3.length).toBe(1);
        expect(likes3[0]).toBe(like3);
    }));

    it('should correctly recognize author of commit', inject(function() {
        // Given
        var emptyCommitData = _emptyCommitData();
        var commit = new codebrag.CurrentCommit(emptyCommitData);

        // Then
        expect(commit.isUserAuthorOfCommit('Author Name')).toBeTruthy();
        expect(commit.isUserAuthorOfCommit('Non-author Name')).toBeFalsy();
    }));

    it('should correctly determine whether a user already liked a line or not', inject(function() {
        // Given
        var emptyCommitData = _emptyCommitData();
        var commit = new codebrag.CurrentCommit(emptyCommitData);
        var like = _randomLike();
        var fileName = 'Beans.scala';
        var lineNumber = 665;
        commit.addLike(like, fileName, lineNumber);

        // Then
        expect(commit.userAlreadyLikedLine('Author Name', fileName, lineNumber)).toBeTruthy();
        expect(commit.userAlreadyLikedLine('Author Name', fileName, lineNumber + 1)).toBeFalsy();
        expect(commit.userAlreadyLikedLine('Other User Name', fileName, lineNumber)).toBeFalsy();
    }));

    it('should add general likes to a commit', inject(function() {
        // Given
        var emptyCommitData = _emptyCommitData();
        var commit = new codebrag.CurrentCommit(emptyCommitData);
        var like1 = _randomLike();
        var like2 = _randomLike();
        commit.addGeneralLike(like1);
        commit.addGeneralLike(like2);

        // Then
        var likes = commit.reactions["likes"];
        expect(likes.length).toBe(2);
        expect(likes[0]).toEqual(like1);
        expect(likes[1]).toEqual(like2);
    }));

    it('should correctly determine whether a user already liked entire commit or not', inject(function() {
        // Given
        var emptyCommitData = _emptyCommitData();
        var commit = new codebrag.CurrentCommit(emptyCommitData);
        var like = _randomLike();
        commit.addGeneralLike(like);

        // Then
        expect(commit.userAlreadyLikedCommit('Author Name')).toBeTruthy();
        expect(commit.userAlreadyLikedCommit('Other User Name')).toBeFalsy();
    }));

    it('should say that user did not like a commit if there are no likes for commit', inject(function() {
        // Given
        var emptyCommitData = _emptyCommitData();
        var commit = new codebrag.CurrentCommit(emptyCommitData);

        // Then
        expect(commit.userAlreadyLikedCommit('Author Name')).toBeFalsy();
    }));

    function _emptyCommitData() {
        return {
            reactions: [],
            lineReactions: [],
            commit: {
                authorName: 'Author Name'
            },
            diff: {},
            suppressedFiles: {}
        };
    }

    function _randomLike() {
        return {
            authorName: 'Author Name',
            irrelevantRandomData: Math.random()
        }
    }
});
