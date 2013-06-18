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
                        $compile(el.find(attrs.compile))(scope);
                        removeWatcher();
                    });
                }
            }
        }
    })

    .directive('lineCommentForm', function($compile, events) {

        var inlineCommentFormTemplate = $('#inlineCommentForm').html(); //$templateCache.get('inlineCommentForm');

        var fileDiffRootSelector = 'table';
        var fileDiffLineSelector = 'tbody';
        var clickableSelector = '[data-commentable]';
        var inlineCommentFormRootSelector = 'tr.comment-form';

        var fileNameDataAttr = 'file-name';
        var lineNumberDataAttr = 'line-number';

        function InlineCommentForm(rowClicked) {

            var fileDiffLine = rowClicked.parents(fileDiffLineSelector);

            this.insert = function(afterFormInsertCallback) {
                fileDiffLine.append(inlineCommentFormTemplate);
                var insertedElement = fileDiffLine.find(inlineCommentFormRootSelector);
                afterFormInsertCallback(insertedElement);
            };

            this.destroy = function(afterFormDestroyCallback) {
                fileDiffLine.find(inlineCommentFormRootSelector).remove();
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


    .directive('lineReactions', function($compile) {

        var lineReactionsTemplate = $('#lineReactions').html();    // templateCache ???

        var fileDiffRootSelector = 'table';
        var lineReactionsSelector = '.inline-comments-container';

        var lineNumberDataAttr = 'line-number';

        function FileReactions(baseElement, fileReactions) {

            this.insert = function(afterDOMInsertCallback) {
                _.forEach(fileReactions, function(lineReactions, lineNumber) {
                    var diffLine = _getCorrespondingLineDOMElement(lineNumber);
                    if(_lineHasNoReactionsYet(diffLine)) {
                        diffLine.append(lineReactionsTemplate);
                        afterDOMInsertCallback(diffLine, lineReactions);
                    }
                });
            };

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
                    $compile(line)(newScope);
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

        return {
            restrict: 'A',
            link: function(scope, el, attrs) {
                var fileDiffRoot = el.closest(fileDiffRootSelector);
                fileDiffRoot.on('click', clickSelector, function(event) {
                    var codeLine = $(event.currentTarget).closest(lineDiffRootSelector);
                    scope.$apply(function(scope) {
                        scope.like(codeLine.data(fileNameDataAttr), codeLine.data(lineNumberDataAttr));
                    });
                });
            }
        }

    })

    .directive('likes', function() {

        return {
            restrict: 'E',
            template: '<span class="username"><i class="icon-heart"></i> Coders who likes this: {{users}}</span>',
            replace: true,
            scope: {
                collection: '='
            },
            link: function(scope, el, attrs) {
                scope.users = _.map(scope.collection, function(like) {
                    return like.authorName;
                }).join(', ');
            }
        }

    });





