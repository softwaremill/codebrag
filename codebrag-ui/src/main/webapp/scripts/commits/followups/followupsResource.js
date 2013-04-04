'use strict';

angular.module('codebrag.commits.followups')

    .factory('Followups', function ($resource) {
        return $resource('rest/followups');
    });

