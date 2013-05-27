/**
 *  THIS IS DEFINITION OF FAKE BACKEND FOR CODEBRAG. ENABLE IT BY ADDING "?nobackend" AT THE END OF URL
 **/

(function(angular) {

    if(!document.URL.match(/\?nobackend$/)) {
        return;
    } else {
        console.log('======== BEWARE!!! USING STUBBED BACKEND ========');
        initialize();
    }

    var pendingCommits = {
        "commits":
            [
                {"id": "519b3364e4b0dff2992c9be5", "sha": "a3258662067616c7c39d2f4b6426b5497ed46d34", "message": "Fix counting of pending commits for notification number\n- function _countBy is not intuitive in this case and can return undefined\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T13:16:04Z", "pendingReview": true},
                {"id": "519b3364e4b0dff2992c9be6", "sha": "77388563ec21d8719da6edd20fb56e21af2b1419", "message": "Implement frontend logic for follow-up counter\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T13:07:52Z", "pendingReview": true},
                {"id": "519b3365e4b0dff2992c9be7", "sha": "b96b18d02e67b8482739fae4c7440b66ff27912e", "message": "Fix test utility to produce an array of commits with correct size\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T12:35:11Z", "pendingReview": true},
                {"id": "519b3365e4b0dff2992c9be8", "sha": "1c983f42a8e1e52ccf1247550549b90051c5c42c", "message": "Fix specs path in jsTestDriver configuration\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T12:26:12Z", "pendingReview": true},
                {"id": "519b3365e4b0dff2992c9be9", "sha": "af1688bf365cf3dfe34e340c5d80ba053b155b2f", "message": "Implement frontend for pending commit counter\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T07:41:45Z", "pendingReview": true},
                {"id": "519b3365e4b0dff2992c9bea", "sha": "055bc2915e951261dc88ee80503d6e6a5d41abde", "message": "Fix test dependencies\n- remove reference to non-existing files which was causing fails\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-14T12:02:08Z", "pendingReview": true},
                {"id": "519b3365e4b0dff2992c9beb", "sha": "3a335098dd49ea0cf1f53dfaa563db02df780596", "message": "Frontend service for loading and updating notification counters\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-14T11:42:30Z", "pendingReview": true},
                {"id": "519b3365e4b0dff2992c9bec", "sha": "7fea045446165cbaf90c4f24e4d440d65afe3c41", "message": "ReST API for notification counters\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-13T12:38:18Z", "pendingReview": true}
            ]
    };

    var allCommits = {
        "commits":
            [
                {"id": "51a31ee3e4b025f047aa0941", "sha": "08793a80d7e267daed16b397518d48c56d4f6c7b", "message": "Add directive for activating elements on specified states\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-23T14:03:18Z", "pendingReview": false},
                {"id": "51a31ee3e4b025f047aa0942", "sha": "cdae3e9f5e4741926188c0ce853ba30e57cfd84a", "message": "Adjust new UI to support autoscroll on follow-ups\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-23T12:59:05Z", "pendingReview": false},
                {"id": "51a31ee3e4b025f047aa0943", "sha": "02e095b6a462ffd2d73c7f484430489edb247aa7", "message": "Apply new styles to follow-up details\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-23T12:11:26Z", "pendingReview": false},
                {"id": "51a31ee3e4b025f047aa0948", "sha": "83aa5ce0f5bf6047bc6fb210abe1b5e15d8617e4", "message": "Merge branch 'master' into pretty_ui\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-23T11:24:09Z", "pendingReview": false},
                {"id": "51a31ee3e4b025f047aa0949", "sha": "1e1ad2f30e9efc24b984f5d3c2e5d6ffb8de13d9", "message": "Add a parameter to markCurrent directive for selecting which state param should be used\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-23T11:23:42Z", "pendingReview": false},
                {"id": "51a31ee3e4b025f047aa094a", "sha": "870c4aab847fd3f9e8b55834782d55a69aee027b", "message": "Fix place of broadcasting of 'user logged in' event\n- It was called only for standard authentication\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-23T09:30:15Z", "pendingReview": false},
                {"id": "51a31ee3e4b025f047aa094b", "sha": "b1c0a4067b5771947486dd247469fbadccec056b", "message": "Remove redundant call on app initialization to avoid duplicate checks\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-23T09:29:03Z", "pendingReview": false},
                {"id": "51a31ee3e4b025f047aa094c", "sha": "420369a7442a20088fc526f36307413d6350c66e", "message": "First cuts of sexy follow-ups\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-23T09:21:24Z", "pendingReview": true},
                {"id": "51a31ee4e4b025f047aa0953", "sha": "04aae22ae774f0b912d7521114aa90a21184c8a7", "message": "Make the notification counter service load data on successful user logon\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-22T20:46:57Z", "pendingReview": true},
                {"id": "51a31ee4e4b025f047aa0954", "sha": "b5934ef7bc73134fe2e36d1eb5525e073e0dbc8e", "message": "Decouple notification counters by using events instead of direct update calls\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-22T20:38:52Z", "pendingReview": true},
                {"id": "519cb89de4b094da13c6cbd6", "sha": "699380adb0e1bd5aaf800d50d1896c3740244d95", "message": "Rewrite diff extraction to use pure jgit instead of gitective\n- former implementation was producing corrupted entries for merges and was much slower\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-22T11:21:35Z", "pendingReview": false},
                {"id": "519cb89de4b094da13c6cbd8", "sha": "36af35bb34684c72e6f8c7993469fab11d7d3295", "message": "Fix decreasing of counter on dismissing a follow-up\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-21T09:11:56Z", "pendingReview": true},
                {"id": "519b3364e4b0dff2992c9be3", "sha": "3bd2d37f869f3b52bcdfc5010e96e4893a7eecfd", "message": "Implement finder for notification counters\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-21T07:26:25Z", "pendingReview": false},
                {"id": "519b3364e4b0dff2992c9be4", "sha": "5b7f38e2649a1528738fe65a06ab50f4b92ea442", "message": "Implement servlet for notification counters with stubbed finder\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T20:34:31Z", "pendingReview": false},
                {"id": "519b3364e4b0dff2992c9be5", "sha": "a3258662067616c7c39d2f4b6426b5497ed46d34", "message": "Fix counting of pending commits for notification number\n- function _countBy is not intuitive in this case and can return undefined\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T13:16:04Z", "pendingReview": false},
                {"id": "519b3364e4b0dff2992c9be6", "sha": "77388563ec21d8719da6edd20fb56e21af2b1419", "message": "Implement frontend logic for follow-up counter\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T13:07:52Z", "pendingReview": false},
                {"id": "519b3365e4b0dff2992c9be7", "sha": "b96b18d02e67b8482739fae4c7440b66ff27912e", "message": "Fix test utility to produce an array of commits with correct size\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T12:35:11Z", "pendingReview": false},
                {"id": "519b3365e4b0dff2992c9be8", "sha": "1c983f42a8e1e52ccf1247550549b90051c5c42c", "message": "Fix specs path in jsTestDriver configuration\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T12:26:12Z", "pendingReview": false},
                {"id": "519b3365e4b0dff2992c9be9", "sha": "af1688bf365cf3dfe34e340c5d80ba053b155b2f", "message": "Implement frontend for pending commit counter\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T07:41:45Z", "pendingReview": false},
                {"id": "519b3365e4b0dff2992c9bea", "sha": "055bc2915e951261dc88ee80503d6e6a5d41abde", "message": "Fix test dependencies\n- remove reference to non-existing files which was causing fails\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-14T12:02:08Z", "pendingReview": false},
                {"id": "519b3365e4b0dff2992c9beb", "sha": "3a335098dd49ea0cf1f53dfaa563db02df780596", "message": "Frontend service for loading and updating notification counters\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-14T11:42:30Z", "pendingReview": false},
                {"id": "519b3365e4b0dff2992c9bec", "sha": "7fea045446165cbaf90c4f24e4d440d65afe3c41", "message": "ReST API for notification counters\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-13T12:38:18Z", "pendingReview": false}
            ]
    };

    var commitDiff = {"commit": {"id": "51a31ee3e4b025f047aa0941", "sha": "08793a80d7e267daed16b397518d48c56d4f6c7b", "message": "Add directive for activating elements on specified states\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-23T14:03:18Z", "pendingReview": true}, "comments": [
        {"id": "51a367e4e4b0c4ce97f9baa9", "authorName": "Michal Ostruszka", "message": "But this is on ***whole diff***", "time": "2013-05-27T14:04:20Z"}
    ], "files": [
        {"filename": "codebrag-ui/src/main/webapp/index.html", "status": "modified", "lines": [
            {"line": "@@ -69,6 +69,8 @@", "lineNumberOriginal": -1, "lineNumberChanged": -1, "lineType": "header", "comments": []},
            {"line": "     &lt;script type=&quot;text/javascript&quot; src=&quot;scripts/common/directives/messagePopup.js&quot;&gt;&lt;/script&gt;", "lineNumberOriginal": 69, "lineNumberChanged": 69, "lineType": "not-changed", "comments": []},
            {"line": "     &lt;script type=&quot;text/javascript&quot; src=&quot;scripts/common/directives/httpRequestTracker.js&quot;&gt;&lt;/script&gt;", "lineNumberOriginal": 70, "lineNumberChanged": 70, "lineType": "not-changed", "comments": []},
            {"line": "     &lt;script type=&quot;text/javascript&quot; src=&quot;scripts/common/directives/isolateClick.js&quot;&gt;&lt;/script&gt;", "lineNumberOriginal": 71, "lineNumberChanged": 71, "lineType": "not-changed", "comments": []},
            {"line": "+    &lt;script type=&quot;text/javascript&quot; src=&quot;scripts/common/directives/activeForStates.js&quot;&gt;&lt;/script&gt;", "lineNumberOriginal": -1, "lineNumberChanged": 72, "lineType": "added", "comments": []},
            {"line": "+", "lineNumberOriginal": -1, "lineNumberChanged": 73, "lineType": "added", "comments": []},
            {"line": "     &lt;script type=&quot;text/javascript&quot; src=&quot;scripts/common/directives/markdownToHtml.js&quot;&gt;&lt;/script&gt;", "lineNumberOriginal": 72, "lineNumberChanged": 74, "lineType": "not-changed", "comments": []},
            {"line": "     &lt;script type=&quot;text/javascript&quot; src=&quot;scripts/common/directives/markCurrent.js&quot;&gt;&lt;/script&gt;", "lineNumberOriginal": 73, "lineNumberChanged": 75, "lineType": "not-changed", "comments": []},
            {"line": "     &lt;script type=&quot;text/javascript&quot; src=&quot;scripts/common/directives/scrollable.js&quot;&gt;&lt;/script&gt;", "lineNumberOriginal": 74, "lineNumberChanged": 76, "lineType": "not-changed", "comments": []},
            {"line": "@@ -92,8 +94,8 @@", "lineNumberOriginal": -1, "lineNumberChanged": -1, "lineType": "header", "comments": []},
            {"line": "         &lt;a href=&quot;#&quot; class=&quot;logo&quot;&gt;&lt;img src=&quot;v2/images/logo.png&quot;&gt;&lt;/a&gt;", "lineNumberOriginal": 92, "lineNumberChanged": 94, "lineType": "not-changed", "comments": []},
            {"line": "         &lt;nav class=&quot;main-nav&quot;&gt;", "lineNumberOriginal": 93, "lineNumberChanged": 95, "lineType": "not-changed", "comments": []},
            {"line": "             &lt;ul class=&quot;button-group&quot; ng-controller=&quot;NotificationCountersCtrl&quot; ng-show=&quot;isLogged()&quot;&gt;", "lineNumberOriginal": 94, "lineNumberChanged": 96, "lineType": "not-changed", "comments": []},
            {"line": "-                &lt;li&gt;&lt;a id=&quot;commits-btn&quot; href=&quot;#/commits&quot; class=&quot;button active&quot;&gt;&lt;span class=&quot;number&quot;&gt;{{counters.commits}} &lt;/span&gt;commits&lt;/a&gt;&lt;/li&gt;", "lineNumberOriginal": 95, "lineNumberChanged": -1, "lineType": "removed", "comments": []},
            {"line": "-                &lt;li&gt;&lt;a id=&quot;followups-btn&quot; href=&quot;#/followups&quot; class=&quot;button&quot;&gt;&lt;span class=&quot;number&quot;&gt;{{counters.followups}} &lt;/span&gt;follow-ups&lt;/a&gt;&lt;/li&gt;", "lineNumberOriginal": 96, "lineNumberChanged": -1, "lineType": "removed", "comments": []},
            {"line": "+                &lt;li&gt;&lt;a id=&quot;commits-btn&quot; href=&quot;#/commits&quot; class=&quot;button&quot; active-for-states=&quot;commits,commits.list,commits.details&quot;&gt;&lt;span class=&quot;number&quot;&gt;{{counters.commits}} &lt;/span&gt;commits&lt;/a&gt;&lt;/li&gt;", "lineNumberOriginal": -1, "lineNumberChanged": 97, "lineType": "added", "comments": []},
            {"line": "+                &lt;li&gt;&lt;a id=&quot;followups-btn&quot; href=&quot;#/followups&quot; class=&quot;button&quot; active-for-states=&quot;followups,followups.list,followups.details&quot;&gt;&lt;span class=&quot;number&quot;&gt;{{counters.followups}} &lt;/span&gt;follow-ups&lt;/a&gt;&lt;/li&gt;", "lineNumberOriginal": -1, "lineNumberChanged": 98, "lineType": "added", "comments": []},
            {"line": "             &lt;/ul&gt;", "lineNumberOriginal": 97, "lineNumberChanged": 99, "lineType": "not-changed", "comments": []},
            {"line": "         &lt;/nav&gt;", "lineNumberOriginal": 98, "lineNumberChanged": 100, "lineType": "not-changed", "comments": []},
            {"line": "         &lt;div class=&quot;user-info&quot; ng-show=&quot;isLogged()&quot;&gt;&lt;span class=&quot;username&quot;&gt;&lt;a  ng-click=&quot;logout()&quot;&gt;Logout&lt;/a&gt;&lt;/span&gt;&lt;/div&gt;", "lineNumberOriginal": 99, "lineNumberChanged": 101, "lineType": "not-changed", "comments": []}
        ]},
        {"filename": "codebrag-ui/src/main/webapp/scripts/common/directives/activeForStates.js", "status": "added", "lines": [
            {"line": "@@ -0,0 +1,29 @@", "lineNumberOriginal": -1, "lineNumberChanged": -1, "lineType": "header", "comments": []},
            {"line": "+angular.module('codebrag.common.directives')", "lineNumberOriginal": -1, "lineNumberChanged": 1, "lineType": "added", "comments": []},
            {"line": "+", "lineNumberOriginal": -1, "lineNumberChanged": 2, "lineType": "added", "comments": []},
            {"line": "+    .directive('activeForStates', function($state) {", "lineNumberOriginal": -1, "lineNumberChanged": 3, "lineType": "added", "comments": []},
            {"line": "+        var state = $state;", "lineNumberOriginal": -1, "lineNumberChanged": 4, "lineType": "added", "comments": []},
            {"line": "+        return {", "lineNumberOriginal": -1, "lineNumberChanged": 5, "lineType": "added", "comments": []},
            {"line": "+            restrict: 'A',", "lineNumberOriginal": -1, "lineNumberChanged": 6, "lineType": "added", "comments": []},
            {"line": "+            link: function(scope, elem, attrs) {", "lineNumberOriginal": -1, "lineNumberChanged": 7, "lineType": "added", "comments": []},
            {"line": "+", "lineNumberOriginal": -1, "lineNumberChanged": 8, "lineType": "added", "comments": []},
            {"line": "+                var activeStates = attrs['activeForStates'].split(',');", "lineNumberOriginal": -1, "lineNumberChanged": 9, "lineType": "added", "comments": [
                {"id": "51a366fae4b025f047aa0a73", "authorName": "Michal Ostruszka", "message": "This is sample inline comment to demonstrate markdown syntax:\n\n    function dummy(value) {\n        alert('this is dummy ' + value);\n    }\n\nThe above is ***the code***", "time": "2013-05-27T14:00:26Z"},
                {"id": "51a367c2e4b0c4ce97f9ba9d", "authorName": "Michal Ostruszka", "message": "This is another comment on the same line", "time": "2013-05-27T14:03:46Z"},
                {"id": "51a367c9e4b0c4ce97f9baa1", "authorName": "Michal Ostruszka", "message": "and yet another", "time": "2013-05-27T14:03:53Z"}
            ]},
            {"line": "+                hookOnEvent();", "lineNumberOriginal": -1, "lineNumberChanged": 10, "lineType": "added", "comments": []},
            {"line": "+                markActive();", "lineNumberOriginal": -1, "lineNumberChanged": 11, "lineType": "added", "comments": []},
            {"line": "+", "lineNumberOriginal": -1, "lineNumberChanged": 12, "lineType": "added", "comments": []},
            {"line": "+                function markActive() {", "lineNumberOriginal": -1, "lineNumberChanged": 13, "lineType": "added", "comments": []},
            {"line": "+                var stateName = state.$current.name;", "lineNumberOriginal": -1, "lineNumberChanged": 14, "lineType": "added", "comments": []},
            {"line": "+                if (_.contains(activeStates, stateName))", "lineNumberOriginal": -1, "lineNumberChanged": 15, "lineType": "added", "comments": []},
            {"line": "+                    elem.addClass(&quot;active&quot;);", "lineNumberOriginal": -1, "lineNumberChanged": 16, "lineType": "added", "comments": []},
            {"line": "+                else", "lineNumberOriginal": -1, "lineNumberChanged": 17, "lineType": "added", "comments": [
                {"id": "51a367d3e4b0c4ce97f9baa5", "authorName": "Michal Ostruszka", "message": "and this is on other line", "time": "2013-05-27T14:04:03Z"}
            ]},
            {"line": "+                    elem.removeClass(&quot;active&quot;);", "lineNumberOriginal": -1, "lineNumberChanged": 18, "lineType": "added", "comments": []},
            {"line": "+                }", "lineNumberOriginal": -1, "lineNumberChanged": 19, "lineType": "added", "comments": []},
            {"line": "+", "lineNumberOriginal": -1, "lineNumberChanged": 20, "lineType": "added", "comments": []},
            {"line": "+            function hookOnEvent() {", "lineNumberOriginal": -1, "lineNumberChanged": 21, "lineType": "added", "comments": []},
            {"line": "+", "lineNumberOriginal": -1, "lineNumberChanged": 22, "lineType": "added", "comments": []},
            {"line": "+            scope.$on('$stateChangeSuccess', function() {", "lineNumberOriginal": -1, "lineNumberChanged": 23, "lineType": "added", "comments": []},
            {"line": "+                markActive();", "lineNumberOriginal": -1, "lineNumberChanged": 24, "lineType": "added", "comments": []},
            {"line": "+            });", "lineNumberOriginal": -1, "lineNumberChanged": 25, "lineType": "added", "comments": []},
            {"line": "+        }   }", "lineNumberOriginal": -1, "lineNumberChanged": 26, "lineType": "added", "comments": []},
            {"line": "+    }", "lineNumberOriginal": -1, "lineNumberChanged": 27, "lineType": "added", "comments": []},
            {"line": "+    });", "lineNumberOriginal": -1, "lineNumberChanged": 28, "lineType": "added", "comments": []},
            {"line": "+", "lineNumberOriginal": -1, "lineNumberChanged": 29, "lineType": "added", "comments": []}
        ]}
    ]};

    var counters = {"pendingCommitCount":8,"followupCount":3};

    var authUser = {
        "id":"517a3debe4b055c40cadecea",
        "login":"john_doe",
        "email":"michal.ostruszka@softwaremill.com",
        "token":"818728e2-ced3-4d58-b676-1003d4106816"
    };

    function initialize() {

        angular.module('codebrag')
            .config(function($provide) {
                $provide.decorator('$httpBackend', angular.mock.e2e.$httpBackendDecorator);
            });

        angular.module('codebrag')
            .run(function($httpBackend) {

                $httpBackend.whenGET(/views\/.*/).passThrough();

                $httpBackend.whenGET('rest/users').respond(authUser);

                $httpBackend.whenGET('rest/notificationCounts').respond(counters);

                $httpBackend.whenGET('rest/commits?filter=pending').respond(pendingCommits);
                $httpBackend.whenGET('rest/commits?filter=all').respond(allCommits);

                $httpBackend.whenGET(/rest\/commits\/[a-z0-9]{12}/).respond(commitDiff);

            });
    }

}(angular));


