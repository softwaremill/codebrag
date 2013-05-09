angular.module('codebrag.commits').
    value('commitLoadFilter',
    {
        modes: {
            all: {
                name: "All",
                value: 'all'
            },
            pending: {
                name: "Pending review",
                value: 'pending'
            }
        },

        current: {
            value: 'pending'
        },

        isAll: function () {
            return this.current == this.modes.all;
        }
    }
);