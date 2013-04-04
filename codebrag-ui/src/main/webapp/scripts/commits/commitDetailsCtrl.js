angular.module('codebrag.commits')

    .controller('CommitDetailsCtrl', function ($scope, currentCommit, Files) {
        $scope.currentCommit = currentCommit;
        $scope.files = [];

        if (currentCommit.isSelected()) {
            Files.get({sha: currentCommit.sha}, function (files) {
                $scope.files = preprocessFiles(files);
            }, function (error) {
                console.error(error);
            });
        }

        function preprocessFiles(files) {
            var numOfFiles = files.length;
            for (var i = 0; i < numOfFiles; i++) {
                var file = files[i];
                var numOfLines = file.lines.length;
                for(var j = 0;j < numOfLines;j++) {
                    var line = file.lines[j];
                    line.line = line.line.replace(" ", "&nbsp;");
                }
            }
            return files;
        }

    });


