angular.module('codebrag.commits')

    .value('commitLoadFilter',

        {
            modes: {
                all: 'all',
                pending: 'pending'
            },
            current: 'pending',


            setPendingMode: function() {
                this.current = this.modes.pending;
            },

            setAllMode: function() {
                this.current = this.modes.all;
            },

            isAll: function () {
                return this.current == this.modes.all;
            }
        }
    );