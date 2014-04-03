var codebrag = codebrag || {};

(function(codebrag) {
    'use strict';

    codebrag.commit = codebrag.commit || {};
    codebrag.commit.mixins = codebrag.commit.mixins || {};

    function withReviewStateMethods() {
        this.isToReview = function() {
            return this.state === 'AwaitingReview';
        };
        this.isReviewNotRequired = function() {
            return this.state === 'ReviewNotRequired';
        };
        this.isAlreadyReviewed = function() {
            return this.state === 'Reviewed';
        }
    }

    codebrag.commit.mixins.withReviewStateMethods = withReviewStateMethods;

})(codebrag);