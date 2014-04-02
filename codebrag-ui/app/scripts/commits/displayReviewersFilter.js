var codebrag = codebrag || {};
codebrag.formatters = codebrag.formatters || {};

codebrag.formatters.reviewersListFormatter = function(currentUser) {

    function currentUserReviewed(list) {
        return list.filter(function(r) {
            return r.id === currentUser.id;
        }).length > 0;
    }

    function formatSingleReviewer(list) {
        var reviewedByCurrentUser = currentUserReviewed(list);
        return reviewedByCurrentUser ? 'You reviewed this commit' : [list[0].fullName, "reviewed this commit"].join(" ");
    }

    function formatMultipleReviewers(list) {
        var reviewedByCurrentUser = currentUserReviewed(list);
        return reviewedByCurrentUser ?
            [list.length, 'people including you reviewed this commit'].join(' ') : [list.length, 'people reviewed this commit'].join(' ');
    }

    return function(list) {
        if(list.length == 0) return 'Awaiting review';
        if(list.length == 1) return formatSingleReviewer(list);
        return formatMultipleReviewers(list);
    };

};

angular.module('codebrag.commits').filter('displayReviewers', function(authService) {
    return codebrag.formatters.reviewersListFormatter(authService.loggedInUser);
});