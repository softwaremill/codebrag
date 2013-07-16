var codebrag = codebrag || {};


/**
 * Caches DOM nodes for all reactions that need to fit diff visible area and resizes accordingly
 * When elements are added to cache all cached elements are resized at the end. Same for removal.
 * On width recalculation there is no resize if width was not changed.
 *
 * Requires: underscore and jquery
 */
codebrag.diffReactionsDOMReferenceCacheAndResizer = {

    collection: [],
    targetWidth: 0,
    resizableElementsSelector: '[data-resize-to-view]',

    clearCache: function() {
        this.collection.length = 0;
    },

    addElementsAndResizeAll: function(startElement) {
        var self = this;
        _.forEach(startElement.find(this.resizableElementsSelector), function(el) {
            self.collection.push(el);
        });
        this.resizeAll();
    },

    removeElementsAndResizeAll: function(startElement) {
        var self = this;
        _.forEach(startElement.find(this.resizableElementsSelector), function(el) {
            self.collection.splice(self.collection.indexOf(el), 1);
        });
        this.resizeAll();
    },

    resizeAll: function() {
        var self = this;
        _.forEach(this.collection, function(el) {
            $(el).width(self.targetWidth);
        })
    },

    recalculateWidthAndResizeAll: function(exampleElement) {
        var newWidth = parseInt(exampleElement.width(), 10);
        if(newWidth !== this.targetWidth) {
            this.targetWidth = newWidth;
            this.resizeAll();
        }
    }

};

angular.module('codebrag.commits')

    .directive('commitDiff', function($compile) {
        return {
            restrict: 'E',
            replace: true,
            compile: function(tEl, tAttrs, transclude) {
                var diffTemplate = Handlebars.compile(tEl.html());
                tEl.html('');
                return function(scope, el, attrs) {
                    var dataAttrName = attrs.data;
                    var removeWatcher = scope.$watch(dataAttrName, function(newVal) {
                        var dataRoot = {};
                        if(angular.isUndefined(newVal)) {
                            return;
                        }
                        dataRoot[dataAttrName] = scope.$eval(dataAttrName);
                        el.html(diffTemplate(dataRoot));
                        codebrag.diffReactionsDOMReferenceCacheAndResizer.clearCache();
                        $compile(el.find(attrs.compile))(scope);
                        removeWatcher();
                    });
                }
            }
        }
    })

    .directive('lineCommentForm', function($compile, events, $templateCache) {

        var inlineCommentFormTemplate = $templateCache.get('inlineCommentForm');

        var fileDiffRootSelector = 'table';
        var fileDiffLineSelector = 'tbody';
        var clickableSelector = '[data-commentable]';
        var inlineCommentFormRootSelector = 'tr.comment-form';
        var inlineCommentsControlsSelector = '[data-thread-controls]';

        var fileNameDataAttr = 'file-name';
        var lineNumberDataAttr = 'line-number';


        function InlineCommentForm(rowClicked) {

            var fileDiffLine = rowClicked.parents(fileDiffLineSelector);
            var insertedElement;

            this.insert = function(afterFormInsertCallback) {
                fileDiffLine.append(inlineCommentFormTemplate);

                fileDiffLine.find(inlineCommentsControlsSelector).hide();

                insertedElement = fileDiffLine.find(inlineCommentFormRootSelector);
                codebrag.diffReactionsDOMReferenceCacheAndResizer.addElementsAndResizeAll(insertedElement);
                afterFormInsertCallback(insertedElement);
            };

            this.destroy = function(afterFormDestroyCallback) {
                codebrag.diffReactionsDOMReferenceCacheAndResizer.removeElementsAndResizeAll(insertedElement);
                fileDiffLine.find(inlineCommentFormRootSelector).remove();
                fileDiffLine.find(inlineCommentsControlsSelector).show();
                afterFormDestroyCallback();
            };

            this.commentParams = function() {
                return {
                    fileName: fileDiffLine.data(fileNameDataAttr),
                    lineNumber: fileDiffLine.data(lineNumberDataAttr)
                }
            };

            this.isAlreadyPresent = function() {
                return fileDiffLine.find(inlineCommentFormRootSelector).length > 0;
            }

        }

        function linkFn(scope, el, attrs) {
            var fileDiffRoot = el.parent(fileDiffRootSelector);
            fileDiffRoot.on('click', clickableSelector, function(event) {
                var elementClicked = $(event.currentTarget);
                var commentForm = new InlineCommentForm(elementClicked);
                if(commentForm.isAlreadyPresent()) {
                    return;
                }
                commentForm.insert(function bindScope(createdElement) {
                    var commentFormScope = scope.$new();
                    commentFormScope.commentParams = commentForm.commentParams();
                    $compile(createdElement)(commentFormScope);
                    commentFormScope.$apply();
                    commentFormScope.$on(events.closeForm, function() {
                        commentForm.destroy(function destroyScope() {
                            commentFormScope.$destroy();
                        });
                    });
                });
            });
        }

        return {
            restrict: 'A',
            link: linkFn
        }
    })


    .directive('lineReactions', function($compile, $templateCache) {

        var lineReactionsTemplate = $templateCache.get('lineReactions');

        var fileDiffRootSelector = 'table';
        var lineReactionsSelector = '[data-inline-reactions-container]';
        var codeRowSelector = '[data-code-row]';

        var lineNumberDataAttr = 'line-number';


        function FileReactions(baseElement, fileReactions) {

            this.insert = function(afterDOMInsertCallback) {
                _.forEach(fileReactions, function(lineReactions, lineNumber) {
                    var diffLine = _getCorrespondingLineDOMElement(lineNumber);
                    var codeRow = diffLine.find(codeRowSelector);
                    if(_lineHasNoReactionsYet(diffLine)) {
                        codeRow.after(lineReactionsTemplate);
                        codebrag.diffReactionsDOMReferenceCacheAndResizer.addElementsAndResizeAll(diffLine);
                        afterDOMInsertCallback(diffLine, lineReactions);
                    }
                    _markLineAsLikedWhenRequired(codeRow, lineReactions);
                });
            };

            function _markLineAsLikedWhenRequired(codeRow, lineReactions) {
                if(lineReactions.likes && lineReactions.likes.length > 0) {
                    codeRow.addClass('liked-by-user');
                }
            }

            function _lineHasNoReactionsYet(line) {
                return line.find(lineReactionsSelector).length === 0;
            }

            function _getCorrespondingLineDOMElement(lineNumber) {
                var lineNumberSelector = ['[data-', lineNumberDataAttr, '="', lineNumber + '"]'].join(""); // create [data-line-number="xx"] selector
                return baseElement.find(lineNumberSelector);
            }

        }

        function linkFn(scope, el, attrs) {

            var fileDiffRoot = el.parent(fileDiffRootSelector);

            scope.$watch(attrs.lineReactions, function(fileReactions) {
                if(angular.isUndefined(fileReactions)) {
                    return;
                }
                var comments = new FileReactions(fileDiffRoot, fileReactions);
                comments.insert(function(line, lineReactions) {
                    var newScope = scope.$new();
                    newScope.lineReactions = lineReactions;
                    $compile(line.find(lineReactionsSelector))(newScope);
                });
            }, true);
        }

        return {
            restrict: 'A',
            link: linkFn
        }

    })

    .directive('lineLike', function() {

        var fileDiffRootSelector = 'table';
        var lineDiffRootSelector = 'tbody';
        var clickSelector = '.like';

        var fileNameDataAttr = 'file-name';
        var lineNumberDataAttr = 'line-number';
        var codeRowSelector = '[data-code-row]';

        return {
            restrict: 'A',
            link: function(scope, el, attrs) {
                var fileDiffRoot = el.closest(fileDiffRootSelector);
                fileDiffRoot.on('click', clickSelector, function(event) {
                    var codeLine = $(event.currentTarget).closest(lineDiffRootSelector);
                    scope.$apply(function(scope) {
                        scope.like(codeLine.data(fileNameDataAttr), codeLine.data(lineNumberDataAttr)).then(function() {
                            codeLine.find(codeRowSelector).addClass('liked-by-user');
                        });
                    });
                });
            }
        }

    })

    .directive('likes', function() {

        return {
            restrict: 'E',
            templateUrl: 'likesLine',
            replace: true,
            transclude: true,
            scope: {
                collection: '='
            }
        }

    })

    .controller('ReactionControlsCtrl', function($scope, $stateParams) {

        var _lineHasComments, _isCurrentFollowup;

        $scope.isCurrentFollowup = function(reactions) {
            if(_isCurrentFollowup) {
                return _isCurrentFollowup;
            }
            var notInFollowup = _.isUndefined($stateParams.followupId);
            if(notInFollowup) {
                return false;
            }
            var allReactions = (reactions.comments || []).concat(reactions.likes || []);
            var reactionsFound = _.filter(allReactions, function(reaction) {
                return reaction.id === $scope.currentFollowup.reaction.reactionId;
            });
            _isCurrentFollowup = reactionsFound.length > 0;
            return _isCurrentFollowup;
        };

        $scope.lineHasComments = function(reactions) {
            if(_lineHasComments) {
                return _lineHasComments
            }
            _lineHasComments = reactions && reactions.comments && reactions.comments.length > 0;
            return _lineHasComments
        };

        $scope.displayLineControls = function(reactions) {
            if(angular.isUndefined(reactions)) {
                return false;
            }
            var lineHasComments = $scope.lineHasComments(reactions);
            var isCurrentFollowup = $scope.isCurrentFollowup(reactions);
            return lineHasComments || isCurrentFollowup;
        };

        $scope.displayEntireCommitControls = function(reactions) {
            if(angular.isUndefined(reactions)) {
                return false;
            }
            return $scope.isCurrentFollowup(reactions);
        };

    })

    .directive('autoFitCommentsWidth', function($window) {

        var diffFileSelector = '.diff-table-wrapper';
        var exampleWidthElement = '#commit-comments-area';

        function doWhenElementPresent(selector, actionFn) {
            var interval = 10;

            // the setTimeout below checks whether element is present in DOM with some delay
            // if element not found it sets up another setTimeout for the same delay to wait
            // it is due to the fact that code may be executed before DOM is fully loaded/rendered
            setTimeout(function() {
                var elementFound = $(selector);
                if(elementFound.length) {
                    actionFn(elementFound);
                } else {
                    setTimeout(function() {
                        doWhenElementPresent(selector, actionFn);
                    }, interval);
                }
            }, interval);
        }

        return {
            restrict: 'A',
            link: function(scope, el, attrs) {
                doWhenElementPresent(diffFileSelector, function() {
                    var exampleEl = el.find(exampleWidthElement);
                    codebrag.diffReactionsDOMReferenceCacheAndResizer.recalculateWidthAndResizeAll(exampleEl);
                    $($window).on('resize', _.debounce(function(){
                        codebrag.diffReactionsDOMReferenceCacheAndResizer.recalculateWidthAndResizeAll(exampleEl);
                    },50));
                });
            }
        }

    });

