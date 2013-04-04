// Libraries
EnvJasmine.loadGlobal(EnvJasmine.libDir + "jquery-1.8.2-min.js");
EnvJasmine.loadGlobal(EnvJasmine.libDir + "angular-1.1.1.js");
EnvJasmine.loadGlobal(EnvJasmine.libDir + "angular-resource-1.1.1.js");
EnvJasmine.loadGlobal(EnvJasmine.libDir + "angular-sanitize-1.1.1.js");
EnvJasmine.loadGlobal(EnvJasmine.libDir + "angular-cookies-1.1.1.js");
EnvJasmine.loadGlobal(EnvJasmine.libDir + "bootstrap.js");
EnvJasmine.loadGlobal(EnvJasmine.libDir + "moment.js");

// Testing libraries
EnvJasmine.loadGlobal(EnvJasmine.testDir + "../lib/require/require-2.0.6.js");
EnvJasmine.loadGlobal(EnvJasmine.testDir + "require.conf.js");
EnvJasmine.loadGlobal(EnvJasmine.testDir + "../lib/angular/angular-mocks-1.1.1.js");

// Application
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "app.js");

EnvJasmine.loadGlobal(EnvJasmine.rootDir + "session/httpAuthInterceptor.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "session/authService.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "session/sessionCtrl.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "session/profileCtrl.js");

EnvJasmine.loadGlobal(EnvJasmine.rootDir + "commits/commitsCtrl.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "commits/filesResource.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "commits/commitsListItemCtrl.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "commits/commitDetailsCtrl.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "commits/currentCommit.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "commits/pendingCommitsResource.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "commits/comments/commentCtrl.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "commits/comments/commentsResource.js");

EnvJasmine.loadGlobal(EnvJasmine.rootDir + "common/filters/dateFormatFilter.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "common/services/flashService.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "common/services/utilService.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "common/ajaxthrobber.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "common/effects.js");

