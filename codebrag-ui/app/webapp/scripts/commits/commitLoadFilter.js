angular.module('codebrag.commits')

    .value('commitLoadFilter',

        {
            MODES: {
                all: 'all',
                pending: 'pending'
            },
            current: 'pending',
            MAX_COMMITS_ON_LIST: 7,


            setPendingMode: function() {
                this.current = this.MODES.pending;
            },

            setAllMode: function() {
                this.current = this.MODES.all;
            },

            maxCommitsOnList: function() {
                return this.MAX_COMMITS_ON_LIST;
            },

            isAll: function () {
                return this.current == this.MODES.all;
            }
        }
    );