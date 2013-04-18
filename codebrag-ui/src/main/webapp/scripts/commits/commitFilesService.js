angular.module('codebrag.commits')

    .factory('commitFilesService', function ($resource) {

        var filesResource = $resource('rest/commits/:id/files', {id: "@id"});

        function loadFilesForCommit(commitId) {
            return filesResource.query({id: commitId});
        }

        return {
            loadFilesForCommit: loadFilesForCommit
        }
    });

