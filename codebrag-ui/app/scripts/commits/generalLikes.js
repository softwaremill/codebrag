
angular.module('codebrag.commits')

    .directive('generalLikes', function(authService, Likes) {

        function GeneralLikesController($scope) {

            var currentUser = authService.loggedInUser;

            $scope.likeOrUnlike = function() {
                if($scope.commit.isUserAuthorOfCommit(currentUser)) {
                    return;
                }
                if($scope.commit.userAlreadyLikedCommit(currentUser)) {
                    unlikeCommit($scope.commit.reactions.likes);
                } else {
                    likeCommit($scope.commit.reactions.likes);
                }
            };

            mix();

            var removeInitialWatcher = $scope.$watch('commit', function(newVal) {
                if(newVal) {
                    $scope.$watch('commit.reactions.likes.length', function(newLength) {
                        if(angular.isUndefined(newLength)) {
                            return;
                        }
                        mix();
                    }, true);
                    removeInitialWatcher();
                }
            });

            function mix() {
                if($scope.commit) {
                    $scope.usersWhoLike = usersWhoLikeText();
                    $scope.likesAvailable = ($scope.usersWhoLike.length > 0);
                    $scope.likesCount = $scope.usersWhoLike.length;
                    $scope.userCannotLike = userCannotLikeCommit();
                    $scope.noLikesYetText = noLikesYetText();
                    $scope.likesLabel = getCorrectLabel();
                }
            }

            function getCorrectLabel() {
                var likesCount = $scope.commit.reactions.likes.length;
                var text;
                switch(likesCount) {
                    case 0:
                        text = noLikesYetText();
                        break;
                    case 1:
                        text = 'likes this commit';
                        break;
                    default:
                        text = 'like this commit';
                        break;
                }
                return text;
            }

            function unlikeCommit(likes) {
                var userLike = _.find(likes, function (like) {
                    return like.authorId === currentUser.id;
                });
                var like = {commitId: $scope.commit.info.id, likeId: userLike.id};
                Likes.delete(like).$then(function () {
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

            function userCannotLikeCommit() {
                return $scope.commit.userAlreadyLikedCommit(currentUser) || $scope.commit.isUserAuthorOfCommit(currentUser);
            }

            function usersWhoLikeText() {
                return $scope.commit.reactions.likes.map(function(like) {
                    return {id: like.id, authorName: like.authorName};
                });
            }

            function noLikesYetText() {
                return $scope.commit.isUserAuthorOfCommit(currentUser) ? "No likes for this commit yet" : "Be the first to like this commit";
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
