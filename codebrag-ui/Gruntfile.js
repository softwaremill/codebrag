'use strict';

module.exports = function (grunt) {

    var proxyRequests = require('grunt-connect-proxy/lib/utils').proxyRequest;
    var liveReload = require('connect-livereload')({port: 9988});

    grunt.initConfig({

        watch: {
            styles: {
                files: ['app/styles/**/*.{styl,css}'],
                tasks: ['stylus:compile']
            },
            templates: {
                files: ['app/views/**/*.html'],
                tasks: ['html2js']
            },
            livereload: {
                options: {
                    livereload: {
                        port: 9988
                    }
                },
                files: [
                    'app/scripts/**/*.js',
                    'tmp/styles/**/*.css',
                    'tmp/scripts/**/*.js',
                    'app/*.html'
                ]
            }
        },

        preprocess: {
            nobackend: {
                src: 'dist/webapp/index.html',
                dest: 'dist/webapp/index.html'
            }
        },

        connect: {
            proxies: [{context: '/rest/', host: 'localhost', port: 8080}],
            options: {
                port: 9090,
                hostname: 'localhost'
            },

            livereload: {
                options: {
                    open: true,
                    middleware: function (connect) {
                        return [
                            proxyRequests,
                            liveReload,
                            connect.static('tmp'),
                            connect.static('app'),
                        ];
                    }
                }
            },
            dist: {
                options: {
                    open: true,
                    middleware: function (connect) {
                        return [
                            proxyRequests,
                            connect.static('dist/webapp')
                        ];
                    },
                    keepalive: true
                }
            }

        },

        stylus: {
            compile: {
                options: {
                    paths: ['app/styles'],
                    urlfunc: 'url',
                    'include css': true,
                    compress: false
                },
                files: {
                    'tmp/styles/index.css': 'app/styles/index.styl'
                }
            }
        },

        html2js: {
            app: {
                options: {
                    base: 'app'
                },
                src: ['app/views/**/*.html'],
                dest: 'tmp/scripts/templates.js',
                module: 'codebrag.templates'
            }
        },


        karma: {
            options: {
                configFile: 'karma-config.js'
            },
            autotest: {
                singleRun: false,
                autoWatch: true,
                reporters: ['progress', 'osx']
            },
            test: {
                singleRun: true
            },
            teamcity: {
                reporters: ['teamcity'],
                singleRun: true
            }
        },

        useminPrepare: {
            html: 'app/index.html',
            options: {
                dest: 'dist/webapp'
            }
        },

        uglify: {
            options: {
                compress: false,
                mangle: false
            }
        },

        usemin: {
            html: ['dist/webapp/index.html'],
            options: {
                dirs: ['dist/webapp']
            }
        },

        copy: {
            index: {
                expand: true,
                src: 'app/*.html',
                dest: 'dist/webapp',
                flatten: true
            },
            assets: {
                expand: true,
                cwd: 'app/assets',
                src: ['images/**/*', 'fonts/**/*'],
                dest: 'dist/webapp/assets'
            }
        },

        clean: {
            dist: ['dist', 'tmp'],
            tmp: 'tmp'
        },

        jshint: {
            options: {
                "sub": true,
                "curly": true,
                "eqeqeq": true,
                "eqnull": true,
                "expr": true,
                "noarg": true,
                "node": true,
                "trailing": true,
                "undef": true,
                "unused": true
            },
            app: {
                options: {
                    "globals": {
                        codebrag: false,
                        angular: false,
                        $: false,
                        jQuery: false,
                        Markdown: false,
                        Handlebars: false,
                        _: false,
                        moment: false
                    }

                },
                files: {
                    src: ['app/scripts/**/*.js']
                }
            },
            tests: {
                options: {
                    "globals": {
                        codebrag: false,
                        angular: false,
                        $: false,
                        _: false,

                        // Jasmine stuff
                        jasmine: false,
                        expect: false,
                        spyOn: false,
                        describe: false,
                        it: false,
                        beforeEach: false,
                        afterEach: false,

                        // Angular mock stuff
                        inject: false,
                        module: false

                    }

                },
                files: {
                    src: ['test/**/*.js']
                }

            }
        }

    });

    require('matchdep').filterDev('grunt-*').forEach(function (dep) {
        grunt.loadNpmTasks(dep);
    });


    grunt.registerTask('server', function(target) {
        if(target === 'dist') {
            return grunt.task.run(['build', 'configureProxies', 'connect:dist']);
        }

        grunt.task.run([
            'clean:tmp',
            'stylus:compile',
            'html2js',
            'configureProxies',
            'connect:livereload',
            'watch'
        ]);
    });

    grunt.registerTask('build', [
        'clean:dist',
        'test:teamcity',
        'stylus:compile',
        'html2js',
        'copy:assets',
        'copy:index',
        'useminPrepare',
        'concat',
        'usemin',
        'preprocess:nobackend'
    ]);

    grunt.registerTask('test', function(target) {
        var tasks = [
            'clean:tmp',
            'stylus:compile',
            'html2js'
        ];
        if(target === 'teamcity') {
            tasks.push('karma:teamcity');
        } else {
            tasks.push('karma:test');
        }
        grunt.task.run(tasks);
    });

    grunt.registerTask('autotest', [
        'clean:tmp',
        'stylus:compile',
        'html2js',
        'karma:autotest'
    ]);

};