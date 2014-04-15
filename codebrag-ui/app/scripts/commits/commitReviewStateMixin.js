var codebrag = codebrag || {};

(function(codebrag) {
    'use strict';

    codebrag.commit = codebrag.commit || {};
    codebrag.commit.mixins = codebrag.commit.mixins || {};

    function withReviewStateMethods() {
        this.isToReview = function() {
            return this.state === 'AwaitingUserReview';
        };
        this.isNotApplicable = function() {
            return this.state === 'NotApplicable';
        }
    }

    codebrag.commit.mixins.withReviewStateMethods = withReviewStateMethods;

})(codebrag);