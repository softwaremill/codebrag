angular.module("codebrag.common.filters")

    .filter("relativeDate", function() { // relative date format e.g. "4 days ago"
        return function(value) {
            return moment(value).fromNow();
        };
    });