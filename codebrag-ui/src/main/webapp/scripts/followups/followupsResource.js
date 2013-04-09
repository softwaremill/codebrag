'use strict';

angular.module('codebrag.followups')

    .factory('Followups', function ($resource) {
        return $resource('rest/followups/:id', {id: "@id"});
    });

