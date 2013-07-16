describe("Local followups list", function () {

    var followups, list;

    beforeEach(function() {
        followups = [
            {"commit": {"commitId": "commit_1"}, "followups": [
                {"followupId": "followup_11", "lastReaction": {"reactionId": "comment_11"}},
                {"followupId": "followup_12", "lastReaction": {"reactionId": "comment_12"}},
                {"followupId": "followup_13", "lastReaction": {"reactionId": "comment_13"}}
            ]},
            {"commit": {"commitId": "commit_2"}, "followups": [
                {"followupId": "followup_21", "lastReaction": {"reactionId": "comment_21"}},
                {"followupId": "followup_22", "lastReaction": {"reactionId": "comment_22"}}
            ]}
        ];
    });

    it('should remove given followup', function() {
        // given
        list = new codebrag.followups.LocalFollowupsList(followups);

        // when
        list.removeOneAndGetNext('followup_12');

        // then
        expect(list.collection[0].followups.map(function(el) {
            return el.followupId;
        })).toEqual(['followup_11', 'followup_13']) ;
    });

    it('should get next followup in the same commit if available', function() {
        // given
        list = new codebrag.followups.LocalFollowupsList(followups);

        // when
        var nextFollowup = list.removeOneAndGetNext('followup_11');

        // then
        expect(nextFollowup.followupId).toEqual('followup_12') ;
    });

    it('should get first followup from next commit when no next followups available in current commit', function() {
        // given
        list = new codebrag.followups.LocalFollowupsList(followups);

        // when
        var nextFollowup = list.removeOneAndGetNext('followup_13');

        // then
        expect(nextFollowup.followupId).toEqual('followup_21') ;
    });

    it('should return null if no other commit and followup present', function() {
        // given
        listWithOneFollowup = [
            {"commit": {"commitId": "commit_1"}, "followups": [
                {"followupId": "followup_1", "lastReaction": {"reactionId": "comment_1"}}
            ]}
        ];
        list = new codebrag.followups.LocalFollowupsList(listWithOneFollowup);

        // when
        var nextFollowup = list.removeOneAndGetNext('followup_1');

        // then
        expect(nextFollowup).toEqual(null) ;
    });

    it('should return previous followup when last one is removed', function() {
        // given
        list = new codebrag.followups.LocalFollowupsList(followups);

        // when
        var nextFollowup = list.removeOneAndGetNext('followup_22');

        // then
        expect(nextFollowup.followupId).toEqual('followup_21') ;

    });

    it('should return last followup from previous commit if tail is removed', function() {
        // given
        list = new codebrag.followups.LocalFollowupsList(followups);

        // when
        var nextFollowup = list.removeOneAndGetNext('followup_22');
        nextFollowup = list.removeOneAndGetNext(nextFollowup.followupId);
        // then
        expect(nextFollowup.followupId).toEqual('followup_13') ;

    });

    it('should remove commit from list when last commit followup is removed', function() {
        // given
        list = new codebrag.followups.LocalFollowupsList(followups);

        // when
        list.removeOneAndGetNext('followup_21');
        list.removeOneAndGetNext('followup_22');

        // then
        expect(list.collection.length).toEqual(1) ;
        expect(list.collection[0]).toEqual(followups[0]) ;
    });

    it('should tell if there are no followups available locally', function() {
        // given
        var EMPTY_LIST = [];

        // when
        list = new codebrag.followups.LocalFollowupsList(EMPTY_LIST);

        // then
        expect(list.hasFollowups()).toBe(false);
    });

    it('should tell if there are available followups', function() {
        // when
        list = new codebrag.followups.LocalFollowupsList(followups);

        // then
        expect(list.hasFollowups()).toBe(true);
    });

    it('should tell how many followups is on list', function() {
        // when
        list = new codebrag.followups.LocalFollowupsList(followups);

        // then
        expect(list.followupsCount()).toBe(5);
    });
});
