/**
 *  THIS IS DEFINITION OF FAKE BACKEND FOR CODEBRAG. ENABLE IT BY ADDING "?nobackend" AT THE END OF URL
 **/


/* global document */
(function (angular) {

    if (!document.URL.match(/\?nobackend$/)) {
        return;
    } else {
        console.log('======== BEWARE!!! USING STUBBED BACKEND ========');
        initialize();
    }

    var pendingCommits = {
        "totalCount": 8,
        "commits": [
            {"id": "519b3364e4b0dff2992c9be6", "sha": "77388563ec21d8719da6edd20fb56e21af2b1419", "message": "Implement frontend logic for follow-up counter\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T13:07:52Z", "pendingReview": true},
            {"id": "519b3365e4b0dff2992c9be7", "sha": "b96b18d02e67b8482739fae4c7440b66ff27912e", "message": "Fix test utility to produce an array of commits with correct size\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T12:35:11Z", "pendingReview": true},
            {"id": "519b3365e4b0dff2992c9be8", "sha": "1c983f42a8e1e52ccf1247550549b90051c5c42c", "message": "Fix specs path in jsTestDriver configuration\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T12:26:12Z", "pendingReview": true},
            {"id": "519b3365e4b0dff2992c9be9", "sha": "af1688bf365cf3dfe34e340c5d80ba053b155b2f", "message": "Implement frontend for pending commit counter\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T07:41:45Z", "pendingReview": true},
            {"id": "519b3365e4b0dff2992c9bea", "sha": "055bc2915e951261dc88ee80503d6e6a5d41abde", "message": "Fix test dependencies\n- remove reference to non-existing files which was causing fails\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-14T12:02:08Z", "pendingReview": true},
            {"id": "519b3365e4b0dff2992c9beb", "sha": "3a335098dd49ea0cf1f53dfaa563db02df780596", "message": "Frontend service for loading and updating notification counters\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-14T11:42:30Z", "pendingReview": true}
        ]
    };

    var additionalCommits = {
        "totalCount": 8,
        "commits": [
            {"id": "119b3365e4b0dff2992c9be9", "sha": "af1688bf365cf3dfe34e340c5d80ba053b155b2f", "message": "Implement frontend for pending commit counter\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-20T07:41:45Z", "pendingReview": true},
            {"id": "219b3365e4b0dff2992c9bea", "sha": "055bc2915e951261dc88ee80503d6e6a5d41abde", "message": "Fix test dependencies\n- remove reference to non-existing files which was causing fails\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-14T12:02:08Z", "pendingReview": true},
            {"id": "319b3365e4b0dff2992c9beb", "sha": "3a335098dd49ea0cf1f53dfaa563db02df780596", "message": "Frontend service for loading and updating notification counters\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-14T11:42:30Z", "pendingReview": true}
        ]
    };

    var allCommits = {
        "totalCount": 8,
        "commits": [
            {"id": "5aa31ee3e4b025f047aa0941", "sha": "08793a80d7e267daed16b397518d48c56d4f6c7b", "message": "Add directive for activating elements on specified states\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-23T14:03:18Z", "pendingReview": false},
            {"id": "5ba31ee3e4b025f047aa0942", "sha": "cdae3e9f5e4741926188c0ce853ba30e57cfd84a", "message": "Adjust new UI to support autoscroll on follow-ups\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-23T12:59:05Z", "pendingReview": false},
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

    var commitDiff = {"commit": {"id": "519b3365e4b0dff2992c9beb", "sha": "3a335098dd49ea0cf1f53dfaa563db02df780596", "message": "Frontend service for loading and updating notification counters\n", "authorName": "Krzysztof Ciesielski", "committerName": "Krzysztof Ciesielski", "date": "2013-05-14T11:42:30Z", "pendingReview": true}, "diff": [
        {"filename": "codebrag-ui/src/main/webapp/scripts/app.js", "status": "modified", "lines": [
            {"line": "@@ -8,23 +8,25 @@", "lineNumberOriginal": "...", "lineNumberChanged": "...", "lineType": "header"},
            {"line": " angular.module('codebrag.auth', []);", "lineNumberOriginal": "8", "lineNumberChanged": "8", "lineType": "not-changed"},
            {"line": " ", "lineNumberOriginal": "9", "lineNumberChanged": "9", "lineType": "not-changed"},
            {"line": " angular.module('codebrag.session', ['ui.compat', 'codebrag.auth']);", "lineNumberOriginal": "10", "lineNumberChanged": "10", "lineType": "not-changed"},
            {"line": "+angular.module('codebrag.notifications', []);", "lineNumberOriginal": "", "lineNumberChanged": "11", "lineType": "added"},
            {"line": " ", "lineNumberOriginal": "11", "lineNumberChanged": "12", "lineType": "not-changed"},
            {"line": " angular.module('codebrag.commits.comments', ['ui.compat']);", "lineNumberOriginal": "12", "lineNumberChanged": "13", "lineType": "not-changed"},
            {"line": "-angular.module('codebrag.commits', ['ngResource', 'codebrag.auth', 'codebrag.commits.comments']);", "lineNumberOriginal": "13", "lineNumberChanged": "", "lineType": "removed"},
            {"line": "+angular.module('codebrag.commits', ['ngResource', 'codebrag.auth', 'codebrag.commits.comments', 'codebrag.notifications']);", "lineNumberOriginal": "", "lineNumberChanged": "14", "lineType": "added"},
            {"line": " ", "lineNumberOriginal": "14", "lineNumberChanged": "15", "lineType": "not-changed"},
            {"line": "-angular.module('codebrag.followups', ['ngResource', 'ui.compat', 'codebrag.auth']);", "lineNumberOriginal": "15", "lineNumberChanged": "", "lineType": "removed"},
            {"line": "+angular.module('codebrag.followups', ['ngResource', 'ui.compat', 'codebrag.auth', 'codebrag.notifications']);", "lineNumberOriginal": "", "lineNumberChanged": "16", "lineType": "added"},
            {"line": " ", "lineNumberOriginal": "16", "lineNumberChanged": "17", "lineType": "not-changed"},
            {"line": " angular.module('codebrag', [", "lineNumberOriginal": "17", "lineNumberChanged": "18", "lineType": "not-changed"},
            {"line": "     'codebrag.auth',", "lineNumberOriginal": "18", "lineNumberChanged": "19", "lineType": "not-changed"},
            {"line": "     'codebrag.common',", "lineNumberOriginal": "19", "lineNumberChanged": "20", "lineType": "not-changed"},
            {"line": "     'codebrag.session',", "lineNumberOriginal": "20", "lineNumberChanged": "21", "lineType": "not-changed"},
            {"line": "     'codebrag.commits',", "lineNumberOriginal": "21", "lineNumberChanged": "22", "lineType": "not-changed"},
            {"line": "-    'codebrag.followups']);", "lineNumberOriginal": "22", "lineNumberChanged": "", "lineType": "removed"},
            {"line": "+    'codebrag.followups',", "lineNumberOriginal": "", "lineNumberChanged": "23", "lineType": "added"},
            {"line": "+    'codebrag.notifications']);", "lineNumberOriginal": "", "lineNumberChanged": "24", "lineType": "added"},
            {"line": " ", "lineNumberOriginal": "23", "lineNumberChanged": "25", "lineType": "not-changed"},
            {"line": " angular.module('codebrag')", "lineNumberOriginal": "24", "lineNumberChanged": "26", "lineType": "not-changed"},
            {"line": "     .run(function(authService) {", "lineNumberOriginal": "25", "lineNumberChanged": "27", "lineType": "not-changed"},
            {"line": "         authService.requestCurrentUser();", "lineNumberOriginal": "26", "lineNumberChanged": "28", "lineType": "not-changed"},
            {"line": "-    })", "lineNumberOriginal": "27", "lineNumberChanged": "", "lineType": "removed"},
            {"line": "+    });", "lineNumberOriginal": "", "lineNumberChanged": "29", "lineType": "added"},
            {"line": " ", "lineNumberOriginal": "28", "lineNumberChanged": "30", "lineType": "not-changed"},
            {"line": " angular.module('codebrag.auth')", "lineNumberOriginal": "29", "lineNumberChanged": "31", "lineType": "not-changed"},
            {"line": "     .config(function($httpProvider) {", "lineNumberOriginal": "30", "lineNumberChanged": "32", "lineType": "not-changed"}
        ], "diffStats": {"added": 6, "removed": 4}},
        {"filename": "codebrag-ui/src/main/webapp/scripts/notifications/notificationCountersService.js", "status": "added", "lines": [
            {"line": "@@ -0,0 +1,49 @@", "lineNumberOriginal": "...", "lineNumberChanged": "...", "lineType": "header"},
            {"line": "+angular.module('codebrag.notifications')", "lineNumberOriginal": "", "lineNumberChanged": "1", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "2", "lineType": "added"},
            {"line": "+    .service('notificationCountersService', function ($http) {", "lineNumberOriginal": "", "lineNumberChanged": "3", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "4", "lineType": "added"},
            {"line": "+        var counterValues = {", "lineNumberOriginal": "", "lineNumberChanged": "5", "lineType": "added"},
            {"line": "+            loaded: false,", "lineNumberOriginal": "", "lineNumberChanged": "6", "lineType": "added"},
            {"line": "+            commits: 0,", "lineNumberOriginal": "", "lineNumberChanged": "7", "lineType": "added"},
            {"line": "+            followups: 0", "lineNumberOriginal": "", "lineNumberChanged": "8", "lineType": "added"},
            {"line": "+        };", "lineNumberOriginal": "", "lineNumberChanged": "9", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "10", "lineType": "added"},
            {"line": "+        function counters() {", "lineNumberOriginal": "", "lineNumberChanged": "11", "lineType": "added"},
            {"line": "+            if (!counterValues.loaded) {", "lineNumberOriginal": "", "lineNumberChanged": "12", "lineType": "added"},
            {"line": "+                _loadCountersFromServer()", "lineNumberOriginal": "", "lineNumberChanged": "13", "lineType": "added"},
            {"line": "+            }", "lineNumberOriginal": "", "lineNumberChanged": "14", "lineType": "added"},
            {"line": "+            return counterValues;", "lineNumberOriginal": "", "lineNumberChanged": "15", "lineType": "added"},
            {"line": "+        }", "lineNumberOriginal": "", "lineNumberChanged": "16", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "17", "lineType": "added"},
            {"line": "+        function updateFollowups(newCount) {", "lineNumberOriginal": "", "lineNumberChanged": "18", "lineType": "added"},
            {"line": "+            counterValues.followups = newCount;", "lineNumberOriginal": "", "lineNumberChanged": "19", "lineType": "added"},
            {"line": "+        }", "lineNumberOriginal": "", "lineNumberChanged": "20", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "21", "lineType": "added"},
            {"line": "+        function updateCommits(newCount) {", "lineNumberOriginal": "", "lineNumberChanged": "22", "lineType": "added"},
            {"line": "+            counterValues.commits = newCount;", "lineNumberOriginal": "", "lineNumberChanged": "23", "lineType": "added"},
            {"line": "+        }", "lineNumberOriginal": "", "lineNumberChanged": "24", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "25", "lineType": "added"},
            {"line": "+        function decreaseCommits() {", "lineNumberOriginal": "", "lineNumberChanged": "26", "lineType": "added"},
            {"line": "+            counterValues.commits--;", "lineNumberOriginal": "", "lineNumberChanged": "27", "lineType": "added"},
            {"line": "+        }", "lineNumberOriginal": "", "lineNumberChanged": "28", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "29", "lineType": "added"},
            {"line": "+        function decreateFollowups() {", "lineNumberOriginal": "", "lineNumberChanged": "30", "lineType": "added"},
            {"line": "+            counterValues.followups--;", "lineNumberOriginal": "", "lineNumberChanged": "31", "lineType": "added"},
            {"line": "+        }", "lineNumberOriginal": "", "lineNumberChanged": "32", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "33", "lineType": "added"},
            {"line": "+        function _loadCountersFromServer() {", "lineNumberOriginal": "", "lineNumberChanged": "34", "lineType": "added"},
            {"line": "+            $http.get('rest/notificationCounts').then(function(response) {", "lineNumberOriginal": "", "lineNumberChanged": "35", "lineType": "added"},
            {"line": "+                counterValues.commits = response.data.pendingCommitCount;", "lineNumberOriginal": "", "lineNumberChanged": "36", "lineType": "added"},
            {"line": "+                counterValues.followups = response.data.followupCount;", "lineNumberOriginal": "", "lineNumberChanged": "37", "lineType": "added"},
            {"line": "+                counterValues.loaded = true;", "lineNumberOriginal": "", "lineNumberChanged": "38", "lineType": "added"},
            {"line": "+            });", "lineNumberOriginal": "", "lineNumberChanged": "39", "lineType": "added"},
            {"line": "+        }", "lineNumberOriginal": "", "lineNumberChanged": "40", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "41", "lineType": "added"},
            {"line": "+        return {", "lineNumberOriginal": "", "lineNumberChanged": "42", "lineType": "added"},
            {"line": "+            counters: counters,", "lineNumberOriginal": "", "lineNumberChanged": "43", "lineType": "added"},
            {"line": "+            updateFollowups: updateFollowups,", "lineNumberOriginal": "", "lineNumberChanged": "44", "lineType": "added"},
            {"line": "+            decreaseCommits: decreaseCommits,", "lineNumberOriginal": "", "lineNumberChanged": "45", "lineType": "added"},
            {"line": "+            decreaseFollowups: decreateFollowups,", "lineNumberOriginal": "", "lineNumberChanged": "46", "lineType": "added"},
            {"line": "+            updateCommits: updateCommits", "lineNumberOriginal": "", "lineNumberChanged": "47", "lineType": "added"},
            {"line": "+        }", "lineNumberOriginal": "", "lineNumberChanged": "48", "lineType": "added"},
            {"line": "+    });", "lineNumberOriginal": "", "lineNumberChanged": "49", "lineType": "added"},
            {"line": "\\ No newline at end of file", "lineNumberOriginal": "0", "lineNumberChanged": "50", "lineType": "not-changed"}
        ], "diffStats": {"added": 49, "removed": 0}},
        {"filename": "codebrag-ui/src/test/JasmineInBrowserSpecRunner.html", "status": "modified", "lines": [
            {"line": "@@ -37,6 +37,10 @@", "lineNumberOriginal": "...", "lineNumberChanged": "...", "lineType": "header"},
            {"line": "     &lt;script type=&quot;text/javascript&quot; src=&quot;../main/webapp/scripts/auth/httpRequestsBuffer.js&quot;&gt;&lt;/script&gt;", "lineNumberOriginal": "37", "lineNumberChanged": "37", "lineType": "not-changed"},
            {"line": "     &lt;script type=&quot;text/javascript&quot; src=&quot;../main/webapp/scripts/auth/loginForm.js&quot;&gt;&lt;/script&gt;", "lineNumberOriginal": "38", "lineNumberChanged": "38", "lineType": "not-changed"},
            {"line": " ", "lineNumberOriginal": "39", "lineNumberChanged": "39", "lineType": "not-changed"},
            {"line": "+    &lt;!-- notifications --&gt;", "lineNumberOriginal": "", "lineNumberChanged": "40", "lineType": "added"},
            {"line": "+    &lt;script type=&quot;text/javascript&quot; src=&quot;../main/webapp/scripts/notifications/notificationCountersCtrl.js&quot;&gt;&lt;/script&gt;", "lineNumberOriginal": "", "lineNumberChanged": "41", "lineType": "added"},
            {"line": "+    &lt;script type=&quot;text/javascript&quot; src=&quot;../main/webapp/scripts/notifications/notificationCountersService.js&quot;&gt;&lt;/script&gt;", "lineNumberOriginal": "", "lineNumberChanged": "42", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "43", "lineType": "added"},
            {"line": "     &lt;!-- session --&gt;", "lineNumberOriginal": "40", "lineNumberChanged": "44", "lineType": "not-changed"},
            {"line": "     &lt;script type=&quot;text/javascript&quot; src=&quot;../main/webapp/scripts/session/sessionCtrl.js&quot;&gt;&lt;/script&gt;", "lineNumberOriginal": "41", "lineNumberChanged": "45", "lineType": "not-changed"},
            {"line": "     &lt;script type=&quot;text/javascript&quot; src=&quot;../main/webapp/scripts/session/profileCtrl.js&quot;&gt;&lt;/script&gt;", "lineNumberOriginal": "42", "lineNumberChanged": "46", "lineType": "not-changed"}
        ], "diffStats": {"added": 4, "removed": 0}},
        {"filename": "codebrag-ui/src/test/unit/specs/notifications/notificationCountersService-spec.js", "status": "added", "lines": [
            {"line": "@@ -0,0 +1,89 @@", "lineNumberOriginal": "...", "lineNumberChanged": "...", "lineType": "header"},
            {"line": "+'use strict';", "lineNumberOriginal": "", "lineNumberChanged": "1", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "2", "lineType": "added"},
            {"line": "+describe(&quot;CommitsListService&quot;, function () {", "lineNumberOriginal": "", "lineNumberChanged": "3", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "4", "lineType": "added"},
            {"line": "+    var $httpBackend;", "lineNumberOriginal": "", "lineNumberChanged": "5", "lineType": "added"},
            {"line": "+    var rootScope;", "lineNumberOriginal": "", "lineNumberChanged": "6", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "7", "lineType": "added"},
            {"line": "+    beforeEach(module('codebrag.notifications'));", "lineNumberOriginal": "", "lineNumberChanged": "8", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "9", "lineType": "added"},
            {"line": "+    beforeEach(inject(function (_$httpBackend_, $rootScope) {", "lineNumberOriginal": "", "lineNumberChanged": "10", "lineType": "added"},
            {"line": "+        $httpBackend = _$httpBackend_;", "lineNumberOriginal": "", "lineNumberChanged": "11", "lineType": "added"},
            {"line": "+        rootScope = $rootScope;", "lineNumberOriginal": "", "lineNumberChanged": "12", "lineType": "added"},
            {"line": "+    }));", "lineNumberOriginal": "", "lineNumberChanged": "13", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "14", "lineType": "added"},
            {"line": "+    afterEach(inject(function (_$httpBackend_) {", "lineNumberOriginal": "", "lineNumberChanged": "15", "lineType": "added"},
            {"line": "+        _$httpBackend_.verifyNoOutstandingExpectation();", "lineNumberOriginal": "", "lineNumberChanged": "16", "lineType": "added"},
            {"line": "+        _$httpBackend_.verifyNoOutstandingRequest();", "lineNumberOriginal": "", "lineNumberChanged": "17", "lineType": "added"},
            {"line": "+    }));", "lineNumberOriginal": "", "lineNumberChanged": "18", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "19", "lineType": "added"},
            {"line": "+    it('should call server only on first load', inject(function (notificationCountersService) {", "lineNumberOriginal": "", "lineNumberChanged": "20", "lineType": "added"},
            {"line": "+        // Given", "lineNumberOriginal": "", "lineNumberChanged": "21", "lineType": "added"},
            {"line": "+        $httpBackend.expectGET('rest/notificationCounts').respond({pendingCommitCount: 0, followupCount: 0});", "lineNumberOriginal": "", "lineNumberChanged": "22", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "23", "lineType": "added"},
            {"line": "+        // When", "lineNumberOriginal": "", "lineNumberChanged": "24", "lineType": "added"},
            {"line": "+        notificationCountersService.counters();", "lineNumberOriginal": "", "lineNumberChanged": "25", "lineType": "added"},
            {"line": "+        $httpBackend.flush();", "lineNumberOriginal": "", "lineNumberChanged": "26", "lineType": "added"},
            {"line": "+        notificationCountersService.counters();", "lineNumberOriginal": "", "lineNumberChanged": "27", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "28", "lineType": "added"},
            {"line": "+        // Then expected server url called only once", "lineNumberOriginal": "", "lineNumberChanged": "29", "lineType": "added"},
            {"line": "+    }));", "lineNumberOriginal": "", "lineNumberChanged": "30", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "31", "lineType": "added"},
            {"line": "+    it('should load counter values returned from server', inject(function (notificationCountersService) {", "lineNumberOriginal": "", "lineNumberChanged": "32", "lineType": "added"},
            {"line": "+        // Given", "lineNumberOriginal": "", "lineNumberChanged": "33", "lineType": "added"},
            {"line": "+        var expectedCommitCount = 15;", "lineNumberOriginal": "", "lineNumberChanged": "34", "lineType": "added"},
            {"line": "+        var expectedFollowupCount = 121;", "lineNumberOriginal": "", "lineNumberChanged": "35", "lineType": "added"},
            {"line": "+        $httpBackend.expectGET('rest/notificationCounts').respond({", "lineNumberOriginal": "", "lineNumberChanged": "36", "lineType": "added"},
            {"line": "+                pendingCommitCount: expectedCommitCount,", "lineNumberOriginal": "", "lineNumberChanged": "37", "lineType": "added"},
            {"line": "+                followupCount: expectedFollowupCount", "lineNumberOriginal": "", "lineNumberChanged": "38", "lineType": "added"},
            {"line": "+            });", "lineNumberOriginal": "", "lineNumberChanged": "39", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "40", "lineType": "added"},
            {"line": "+        // When", "lineNumberOriginal": "", "lineNumberChanged": "41", "lineType": "added"},
            {"line": "+        var counters = notificationCountersService.counters();", "lineNumberOriginal": "", "lineNumberChanged": "42", "lineType": "added"},
            {"line": "+        $httpBackend.flush();", "lineNumberOriginal": "", "lineNumberChanged": "43", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "44", "lineType": "added"},
            {"line": "+        // Then", "lineNumberOriginal": "", "lineNumberChanged": "45", "lineType": "added"},
            {"line": "+        expect(counters.commits).toEqual(expectedCommitCount);", "lineNumberOriginal": "", "lineNumberChanged": "46", "lineType": "added"},
            {"line": "+        expect(counters.followups).toEqual(expectedFollowupCount);", "lineNumberOriginal": "", "lineNumberChanged": "47", "lineType": "added"},
            {"line": "+    }));", "lineNumberOriginal": "", "lineNumberChanged": "48", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "49", "lineType": "added"},
            {"line": "+    it('should correctly update counter values', inject(function (notificationCountersService) {", "lineNumberOriginal": "", "lineNumberChanged": "50", "lineType": "added"},
            {"line": "+        // Given", "lineNumberOriginal": "", "lineNumberChanged": "51", "lineType": "added"},
            {"line": "+        var expectedCommitCount = 15;", "lineNumberOriginal": "", "lineNumberChanged": "52", "lineType": "added"},
            {"line": "+        var expectedFollowupCount = 121;", "lineNumberOriginal": "", "lineNumberChanged": "53", "lineType": "added"},
            {"line": "+        $httpBackend.expectGET('rest/notificationCounts').respond({", "lineNumberOriginal": "", "lineNumberChanged": "54", "lineType": "added"},
            {"line": "+            pendingCommitCount: 1,", "lineNumberOriginal": "", "lineNumberChanged": "55", "lineType": "added"},
            {"line": "+            followupCount: 2", "lineNumberOriginal": "", "lineNumberChanged": "56", "lineType": "added"},
            {"line": "+        });", "lineNumberOriginal": "", "lineNumberChanged": "57", "lineType": "added"},
            {"line": "+        var counters = notificationCountersService.counters();", "lineNumberOriginal": "", "lineNumberChanged": "58", "lineType": "added"},
            {"line": "+        $httpBackend.flush();", "lineNumberOriginal": "", "lineNumberChanged": "59", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "60", "lineType": "added"},
            {"line": "+        // When", "lineNumberOriginal": "", "lineNumberChanged": "61", "lineType": "added"},
            {"line": "+        notificationCountersService.updateCommits(expectedCommitCount);", "lineNumberOriginal": "", "lineNumberChanged": "62", "lineType": "added"},
            {"line": "+        notificationCountersService.updateFollowups(expectedFollowupCount);", "lineNumberOriginal": "", "lineNumberChanged": "63", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "64", "lineType": "added"},
            {"line": "+        // Then", "lineNumberOriginal": "", "lineNumberChanged": "65", "lineType": "added"},
            {"line": "+        expect(counters.commits).toEqual(expectedCommitCount);", "lineNumberOriginal": "", "lineNumberChanged": "66", "lineType": "added"},
            {"line": "+        expect(counters.followups).toEqual(expectedFollowupCount);", "lineNumberOriginal": "", "lineNumberChanged": "67", "lineType": "added"},
            {"line": "+    }));", "lineNumberOriginal": "", "lineNumberChanged": "68", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "69", "lineType": "added"},
            {"line": "+    it('should correctly decrease counter values', inject(function (notificationCountersService) {", "lineNumberOriginal": "", "lineNumberChanged": "70", "lineType": "added"},
            {"line": "+        // Given", "lineNumberOriginal": "", "lineNumberChanged": "71", "lineType": "added"},
            {"line": "+        var initialCommitCount = 15;", "lineNumberOriginal": "", "lineNumberChanged": "72", "lineType": "added"},
            {"line": "+        var initialFollowupCount = 121;", "lineNumberOriginal": "", "lineNumberChanged": "73", "lineType": "added"},
            {"line": "+        $httpBackend.expectGET('rest/notificationCounts').respond({", "lineNumberOriginal": "", "lineNumberChanged": "74", "lineType": "added"},
            {"line": "+            pendingCommitCount: initialCommitCount,", "lineNumberOriginal": "", "lineNumberChanged": "75", "lineType": "added"},
            {"line": "+            followupCount: initialFollowupCount", "lineNumberOriginal": "", "lineNumberChanged": "76", "lineType": "added"},
            {"line": "+        });", "lineNumberOriginal": "", "lineNumberChanged": "77", "lineType": "added"},
            {"line": "+        var counters = notificationCountersService.counters();", "lineNumberOriginal": "", "lineNumberChanged": "78", "lineType": "added"},
            {"line": "+        $httpBackend.flush();", "lineNumberOriginal": "", "lineNumberChanged": "79", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "80", "lineType": "added"},
            {"line": "+        // When", "lineNumberOriginal": "", "lineNumberChanged": "81", "lineType": "added"},
            {"line": "+        notificationCountersService.decreaseCommits();", "lineNumberOriginal": "", "lineNumberChanged": "82", "lineType": "added"},
            {"line": "+        notificationCountersService.decreaseFollowups();", "lineNumberOriginal": "", "lineNumberChanged": "83", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "84", "lineType": "added"},
            {"line": "+        // Then", "lineNumberOriginal": "", "lineNumberChanged": "85", "lineType": "added"},
            {"line": "+        expect(counters.commits).toEqual(initialCommitCount - 1);", "lineNumberOriginal": "", "lineNumberChanged": "86", "lineType": "added"},
            {"line": "+        expect(counters.followups).toEqual(initialFollowupCount - 1);", "lineNumberOriginal": "", "lineNumberChanged": "87", "lineType": "added"},
            {"line": "+    }));", "lineNumberOriginal": "", "lineNumberChanged": "88", "lineType": "added"},
            {"line": "+});", "lineNumberOriginal": "", "lineNumberChanged": "89", "lineType": "added"}
        ], "diffStats": {"added": 89, "removed": 0}},
        {"filename": "codebrag-ui/src/test/unit/test.dependencies.js", "status": "modified", "lines": [
            {"line": "@@ -31,6 +31,10 @@", "lineNumberOriginal": "...", "lineNumberChanged": "...", "lineType": "header"},
            {"line": " EnvJasmine.loadGlobal(EnvJasmine.rootDir + &quot;session/sessionCtrl.js&quot;);", "lineNumberOriginal": "31", "lineNumberChanged": "31", "lineType": "not-changed"},
            {"line": " EnvJasmine.loadGlobal(EnvJasmine.rootDir + &quot;session/profileCtrl.js&quot;);", "lineNumberOriginal": "32", "lineNumberChanged": "32", "lineType": "not-changed"},
            {"line": " ", "lineNumberOriginal": "33", "lineNumberChanged": "33", "lineType": "not-changed"},
            {"line": "+&lt;!-- notifications --&gt;", "lineNumberOriginal": "", "lineNumberChanged": "34", "lineType": "added"},
            {"line": "+EnvJasmine.loadGlobal(EnvJasmine.rootDir + &quot;notifications/notificationCountersService.js&quot;);", "lineNumberOriginal": "", "lineNumberChanged": "35", "lineType": "added"},
            {"line": "+EnvJasmine.loadGlobal(EnvJasmine.rootDir + &quot;notifications/notificationCountersCtrl.js&quot;);", "lineNumberOriginal": "", "lineNumberChanged": "36", "lineType": "added"},
            {"line": "+", "lineNumberOriginal": "", "lineNumberChanged": "37", "lineType": "added"},
            {"line": " &lt;!-- commits --&gt;", "lineNumberOriginal": "34", "lineNumberChanged": "38", "lineType": "not-changed"},
            {"line": " EnvJasmine.loadGlobal(EnvJasmine.rootDir + &quot;commits/commitLoadFilter.js&quot;);", "lineNumberOriginal": "35", "lineNumberChanged": "39", "lineType": "not-changed"},
            {"line": " EnvJasmine.loadGlobal(EnvJasmine.rootDir + &quot;commits/commitsCtrl.js&quot;);", "lineNumberOriginal": "36", "lineNumberChanged": "40", "lineType": "not-changed"}
        ], "diffStats": {"added": 4, "removed": 0}}
    ], "supressedFiles": [
        {filename: "codebrag-ui/src/main/webapp/v2/index.css", diffStats: {added: 6002, removed: 403}, status: "modified"},
        {filename: "codebrag-ui/src/main/webapp/v2/index.css", diffStats: {added: 6002, removed: 403}, status: "modified"},
        {filename: "codebrag-ui/src/main/webapp/v2/index.css", diffStats: {added: 6002, removed: 403}, status: "modified"},
        {filename: "codebrag-ui/src/main/webapp/v2/index.css", diffStats: {added: 6002, removed: 403}, status: "modified"},
        {filename: "codebrag-ui/src/main/webapp/v2/index.css", diffStats: {added: 6002, removed: 403}, status: "modified"}
    ], "comments": [
        {"id": "51acbed2e4b00ef6d5383f62", "authorName": "Michal Ostruszka", "message": "Sweet chupa chups chocolate cake gummies drag&eacute;e pudding faworki gummies. Applicake macaroon ice cream cookie gingerbread topping jelly. Tart pastry cheesecake chocolate bar bonbon jelly apple pie. Macaroon cotton candy wafer cake macaroon gummi bears candy. Souffl&eacute; lemon drops candy tiramisu. Tart sesame snaps sesame snaps danish souffl&eacute; oat cake jelly jelly beans. Jelly beans muffin brownie dessert. Marzipan jelly beans bear claw lollipop tootsie roll cake topping biscuit chocolate cake.", "time": "2013-06-03T16:05:38Z"},
        {"id": "51acbed4e4b00ef6d5383f66", "authorName": "Michal Ostruszka", "message": "Sweet chupa chups chocolate cake gummies drag&eacute;e pudding faworki gummies. Applicake macaroon ice cream cookie gingerbread topping jelly. Tart pastry cheesecake chocolate bar bonbon jelly apple pie. Macaroon cotton candy wafer cake macaroon gummi bears candy. Souffl&eacute; lemon drops candy tiramisu. Tart sesame snaps sesame snaps danish souffl&eacute; oat cake jelly jelly beans. Jelly beans muffin brownie dessert. Marzipan jelly beans bear claw lollipop tootsie roll cake topping biscuit chocolate cake.", "time": "2013-06-03T16:05:40Z"}
    ], "lineReactions": {"codebrag-ui/src/main/webapp/scripts/app.js": {"25": {
        "likes": [
            {"id": "51deb248300413ead5f708c3", "authorName": "Michal Ostruszka 1"},
            {"id": "51acbecce4b00ef6d5383f5a", "authorName": "Michal Ostruszka 2"},
            {"id": "51acbecfe4b00ef6d5383f5e", "authorName": "Michal Ostruszka 3"}
        ],
        "comments": [
            {"id": "51acbec9e4b00ef6d5383f56", "authorName": "Michal Ostruszka", "message": "Sweet chupa chups chocolate cake gummies ", "time": "2013-06-03T16:05:29Z"},
            {"id": "51acbecce4b00ef6d5383f5a", "authorName": "Michal Ostruszka", "message": "Sweet chupa chups chocolate cake gummies ", "time": "2013-06-03T16:05:32Z"},
            {"id": "51acbecfe4b00ef6d5383f5e", "authorName": "Michal Ostruszka", "message": "Sweet chupa chups chocolate cake gummies ", "time": "2013-06-03T16:05:35Z"}
        ]}}}, "inlineComments": {"codebrag-ui/src/main/webapp/scripts/app.js": {"25": [
        {"id": "51acbec9e4b00ef6d5383f56", "authorName": "Michal Ostruszka", "message": "Sweet chupa chups chocolate cake gummies drag&eacute;e pudding faworki gummies. Applicake macaroon ice cream cookie gingerbread topping jelly. Tart pastry cheesecake chocolate bar bonbon jelly apple pie. Macaroon cotton candy wafer cake macaroon gummi bears candy. Souffl&eacute; lemon drops candy tiramisu. Tart sesame snaps sesame snaps danish souffl&eacute; oat cake jelly jelly beans. Jelly beans muffin brownie dessert. Marzipan jelly beans bear claw lollipop tootsie roll cake topping biscuit chocolate cake.", "time": "2013-06-03T16:05:29Z"},
        {"id": "51acbecce4b00ef6d5383f5a", "authorName": "Michal Ostruszka", "message": "Sweet chupa chups chocolate cake gummies drag&eacute;e pudding faworki gummies. Applicake macaroon ice cream cookie gingerbread topping jelly. Tart pastry cheesecake chocolate bar bonbon jelly apple pie. Macaroon cotton candy wafer cake macaroon gummi bears candy. Souffl&eacute; lemon drops candy tiramisu. Tart sesame snaps sesame snaps danish souffl&eacute; oat cake jelly jelly beans. Jelly beans muffin brownie dessert. Marzipan jelly beans bear claw lollipop tootsie roll cake topping biscuit chocolate cake.", "time": "2013-06-03T16:05:32Z"},
        {"id": "51acbecfe4b00ef6d5383f5e", "authorName": "Michal Ostruszka", "message": "Sweet chupa chups chocolate cake gummies drag&eacute;e pudding faworki gummies. Applicake macaroon ice cream cookie gingerbread topping jelly. Tart pastry cheesecake chocolate bar bonbon jelly apple pie. Macaroon cotton candy wafer cake macaroon gummi bears candy. Souffl&eacute; lemon drops candy tiramisu. Tart sesame snaps sesame snaps danish souffl&eacute; oat cake jelly jelly beans. Jelly beans muffin brownie dessert. Marzipan jelly beans bear claw lollipop tootsie roll cake topping biscuit chocolate cake.", "time": "2013-06-03T16:05:35Z"}
    ], "18": [
        {"id": "51acbeb8e4b00ef6d5383f4e", "authorName": "Michal Ostruszka", "message": "Halvah tootsie roll pie sugar plum pudding lemon drops croissant. Caramels tootsie roll jelly jelly-o. Macaroon pastry wypas cheesecake wafer marshmallow donut jelly-o faworki. Donut muffin marzipan toffee. Sesame snaps marshmallow tootsie roll bear claw. Sweet danish marzipan wypas. Danish jujubes cotton candy gummi bears sesame snaps cake. Tiramisu gummies toffee faworki liquorice cupcake drag&eacute;e dessert. Lemon drops sweet roll souffl&eacute; toffee jujubes cupcake drag&eacute;e jelly beans. Wypas liquorice drag&eacute;e halvah powder.", "time": "2013-06-03T16:05:12Z"},
        {"id": "51acbec4e4b00ef6d5383f52", "authorName": "Michal Ostruszka", "message": "Sweet chupa chups chocolate cake gummies drag&eacute;e pudding faworki gummies. Applicake macaroon ice cream cookie gingerbread topping jelly. Tart pastry cheesecake chocolate bar bonbon jelly apple pie. Macaroon cotton candy wafer cake macaroon gummi bears candy. Souffl&eacute; lemon drops candy tiramisu. Tart sesame snaps sesame snaps danish souffl&eacute; oat cake jelly jelly beans. Jelly beans muffin brownie dessert. Marzipan jelly beans bear claw lollipop tootsie roll cake topping biscuit chocolate cake.", "time": "2013-06-03T16:05:24Z"}
    ]}}};


    var counters = {"pendingCommitCount": 8, "followupCount": 3};

    var authUser = {
        "id": "517a3debe4b055c40cadecea",
        "login": "john_doe",
        "email": "michal.ostruszka@softwaremill.com",
        "token": "818728e2-ced3-4d58-b676-1003d4106816",
        "settings": {
            "appTourDone": false
        }
    };

    var followups = {"followupsByCommit": [
        {"commit": {"commitId": "51cd63b33004edd49fbefb3b", "authorName": "Michal Ostruszka", "message": "Styling of KTHXBYE button changed according to mockup\n", "date": "2013-06-26T14:25:05Z"}, "followups": [
            {"followupId": "51deb248300413ead5f708c5", "lastReaction": {"reactionId": "51deb248300413ead5f708c3", "reactionAuthor": "Happy Coder", "date": "2013-07-11T13:25:28Z", "reactionAuthorAvatarUrl": "https://softwaremill.com/wp-content/uploads/2013/04/puchta.jpg"}, "allReactions": ["51deb248300413ead5f708c3"]},
            {"followupId": "51deb0ef300413ead5f708c2", "lastReaction": {"reactionId": "51deb0ef300413ead5f708c0", "reactionAuthor": "Happy Coder", "date": "2013-07-11T13:19:43Z", "reactionAuthorAvatarUrl": "https://softwaremill.com/wp-content/uploads/2013/04/puchta.jpg"}, "allReactions": ["51deb0ef300413ead5f708c0"]},
            {"followupId": "51deb0ee300413ead5f708bf", "lastReaction": {"reactionId": "51deb0ee300413ead5f708bd", "reactionAuthor": "Happy Coder", "date": "2013-07-11T13:19:42Z", "reactionAuthorAvatarUrl": "https://softwaremill.com/wp-content/uploads/2013/04/puchta.jpg"}, "allReactions": ["51deb0ee300413ead5f708bd"]},
            {"followupId": "51deb0ee300413ead5f708bc", "lastReaction": {"reactionId": "51deb0ee300413ead5f708ba", "reactionAuthor": "Happy Coder", "date": "2013-07-11T13:19:42Z", "reactionAuthorAvatarUrl": "https://softwaremill.com/wp-content/uploads/2013/04/puchta.jpg"}, "allReactions": ["51deb0ee300413ead5f708ba"]},
            {"followupId": "51deb0e1300413ead5f708b9", "lastReaction": {"reactionId": "51deb0e1300413ead5f708b7", "reactionAuthor": "Happy Coder", "date": "2013-07-11T13:19:29Z", "reactionAuthorAvatarUrl": "https://softwaremill.com/wp-content/uploads/2013/04/puchta.jpg"}, "allReactions": ["51deb0e1300413ead5f708b7"]},
            {"followupId": "51deb0e0300413ead5f708b6", "lastReaction": {"reactionId": "51deb0e0300413ead5f708b4", "reactionAuthor": "Happy Coder", "date": "2013-07-11T13:19:28Z", "reactionAuthorAvatarUrl": "https://softwaremill.com/wp-content/uploads/2013/04/puchta.jpg"}, "allReactions": ["51deb0e0300413ead5f708b4"]}
        ]},
        {"commit": {"commitId": "51cd63b33004edd49fbefb3f", "authorName": "Michal Ostruszka", "message": "Add Reply and KTHXBYE buttons to inline threads\n", "date": "2013-06-26T12:58:24Z"}, "followups": [
            {"followupId": "51de73b6300437a6de66535e", "lastReaction": {"reactionId": "51de73b6300437a6de66535c", "reactionAuthor": "Evil Coder", "date": "2013-07-11T08:58:30Z", "reactionAuthorAvatarUrl": ""}, "allReactions": ["51de73b6300437a6de66535c"]},
            {"followupId": "51de73b4300437a6de66535b", "lastReaction": {"reactionId": "51de73b4300437a6de665359", "reactionAuthor": "Evil Coder", "date": "2013-07-11T08:58:28Z", "reactionAuthorAvatarUrl": ""}, "allReactions": ["51de73b4300437a6de665359"]}
        ]}
    ]};

    var followup = {"followupId":"51deb248300413ead5f708c5","date":"2013-07-11T13:25:28Z","commit":{"commitId":"51cd63b33004edd49fbefb3b","authorName":"Michal Ostruszka","message":"Styling of KTHXBYE button changed according to mockup\n","date":"2013-06-26T14:25:05Z"},"reaction":{"reactionId":"51deb248300413ead5f708c3","reactionAuthor":"Happy Coder","reactionAuthorAvatarUrl":"https://softwaremill.com/wp-content/uploads/2013/04/puchta.jpg"}};

    function initialize() {

        angular.module('codebrag')
            .config(function ($provide) {
                $provide.decorator('$httpBackend', angular.mock.e2e.$httpBackendDecorator);
            });

        angular.module('codebrag')
            .run(function ($httpBackend) {

                $httpBackend.whenGET(/views\/.*/).passThrough();

                $httpBackend.whenGET('rest/config/').respond({demo: false, emailNotifications: true});

                $httpBackend.whenGET('rest/users').respond(authUser);

                $httpBackend.whenGET('rest/users/first-registration').respond({firstRegistration: false});

                $httpBackend.whenGET('rest/notificationCounts').respond(counters);

                $httpBackend.whenGET('rest/commits?context=true&limit=7').respond(allCommits);
                $httpBackend.whenGET('rest/commits?filter=to_review&limit=7').respond(pendingCommits);
                $httpBackend.whenGET(/rest\/commits\?filter=to_review&limit=7&min_id=[a-z0-9]{12}/).respond(additionalCommits);
                $httpBackend.whenGET(/rest\/commits\?filter=to_review&limit=1&min_id=[a-z0-9]{12}/).respond(pendingCommits);

                $httpBackend.whenGET(/rest\/commits\/[a-z0-9]{12}/).respond(commitDiff);

                $httpBackend.whenGET('rest/followups/').respond(followups);

                $httpBackend.whenGET(/rest\/followups\/[a-z0-9]{12}/).respond(followup);

                $httpBackend.whenGET('/rest/repoStatus').respond({"repoStatus":{"repositoryName":"codebrag","headId":"33ddf03d675c9c49910da4ea65e18ddc205051ba","ready":true}});

                $httpBackend.whenGET('rest/updates').respond({"lastUpdate":1384251660217,"commits":0,"followups":0});

                $httpBackend.whenPUT('rest/users/settings').respond({userSettings: {}});

                $httpBackend.whenGET('rest/users/settings').respond({userSettings: {}});
            });
    }

}(angular));


