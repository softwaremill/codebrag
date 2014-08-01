angular.module('codebrag.branches')

    .factory('RepoBranch', function() {

        var RepoBranch = function(data) {
            this.name = data.branchName;
            this.watching = data.watching || false;
        };

        return RepoBranch;
    });

