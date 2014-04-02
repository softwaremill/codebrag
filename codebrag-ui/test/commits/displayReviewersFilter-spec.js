'use strict';

describe("Displaying reviewers list", function () {

    var currentUser = reviewer(100, 'Me');
    var Bob = reviewer(10, 'Bob');
    var Alice = reviewer(10, 'Alice');
    var John = reviewer(10, 'John');
    var formatter = codebrag.formatters.reviewersListFormatter(currentUser);

    var testData = [
        {reviewers: [], expected: 'Awaiting review'},
        {reviewers: [currentUser], expected: 'You reviewed this commit'},
        {reviewers: [currentUser, Bob], expected: '2 people including you reviewed this commit'},
        {reviewers: [Bob, Alice, John], expected: '3 people reviewed this commit'}
    ];

    testData.forEach(runTest);

    function runTest(data) {
        it('should format label for: ' + data.reviewers, function() {
            expect(formatter(data.reviewers)).toBe(data.expected);
        });
    }

    function reviewer(id, name) {
        return {id: id, fullName: name, toString: function() { return '(' + this.fullName + ', ' + this.id + ')'}};
    }
});
