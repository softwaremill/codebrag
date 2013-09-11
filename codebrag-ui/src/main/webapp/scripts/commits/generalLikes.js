
angular.module('codebrag.commits')

    .directive('generalLikes', function(authService, Likes) {

        function GeneralLikesController($scope) {

            var currentUserName = authService.loggedInUser.fullName;

            $scope.userCannotLike = function() {
                if(_.isUndefined($scope.commit)) {
                    return false;
                }
                return $scope.commit.userAlreadyLikedCommit(currentUserName) || $scope.commit.isUserAuthorOfCommit(currentUserName);
            };

            $scope.likeOrUnlike = function() {
                if($scope.commit.isUserAuthorOfCommit(currentUserName)) {
                    return;
                }
                if($scope.commit.userAlreadyLikedCommit(currentUserName)) {
                    unlikeCommit($scope.commit.reactions.likes);
                } else {
                    likeCommit($scope.commit.reactions.likes);
                }
            };

            $scope.listUsersWhoLike = function() {
                if(_.isUndefined($scope.commit)) {
                    return '';
                }
                return $scope.commit.reactions.likes.length ? usersWhoLikeText() : noLikesYetText();
            };

            function unlikeCommit(likes) {
                var userLike = _.find(likes, function (like) {
                    return like.authorId === authService.loggedInUser.id;
                });
                var like = {commitId: $scope.commit.info.id, likeId: userLike.id};
                Likes.delete(like).$then(function (resp) {
                    likes.splice(likes.indexOf(userLike), 1);
                });
                return like;
            }

            function likeCommit(likes) {
                var like = {commitId: $scope.commit.info.id};
                Likes.save(like).$then(function (resp) {
                    likes.push(resp.data);
                });
                return like;
            }

            function usersWhoLikeText() {
                var userNames = $scope.commit.reactions.likes.map(function(like) {
                    return like.authorName;
                });
                var namesJoined = userNames.join(', ');
                return userNames.length == 1 ? (namesJoined + ' likes this commit') : (namesJoined + ' like this commit');
            }

            function noLikesYetText() {
                return $scope.commit.isUserAuthorOfCommit(currentUserName) ? "No likes for this commit yet" : "Be the first to like this commit";
            }

        }

        return {
            restrict: 'E',
            replace: true,
            scope: {
                commit: '='
            },
            templateUrl: 'views/generalLikes.html',
            controller: GeneralLikesController
        };
    });
