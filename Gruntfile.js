var jsfiles = ['frontend/js/variables.js','frontend/js/style.js', 'frontend/js/packets.js', 'frontend/js/socket.js', 'frontend/js/action.js'];

module.exports = function(grunt) {
    require('jit-grunt')(grunt);

    grunt.initConfig({
        //compile less
        less: {
            development: {
                options: {
                    compress: true,
                    yuicompress: true,
                    optimization: 2
                },
                files: {
                    'docs/css/style.min.css': 'frontend/less/style.less' // destination file and source file
                }
            }
        },
        //minify js and combine into one file
        uglify: {
            //TODO combine js to one file

            my_target: {
                files: {
                    'docs/js/ouch.min.js': jsfiles,
                }
            }
        },
        //minify html
        minifyHtml: {
            options: {
                cdata: true
            },
            dist: {
                files: {
                    'docs/index.html': 'frontend/index.html'
                }
            }
        },
        watch: {
            styles: {
                files: ['frontend/less/**/*.less'], // which files to watch
                tasks: ['less'],
                options: {
                    nospawn: true
                }
            },
            javascript: {
                files: ['frontend/js/**/*.js'], // which files to watch
                tasks: ['uglify'],
            },
            html: {
                files: ['frontend/index.html'],
                task: ['minifyHtml'],
            }
        }
    });

    grunt.registerTask('default', ['less', 'uglify', 'minifyHtml', 'watch']);
};