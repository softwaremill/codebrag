angular.module('codebrag.commits')

    .value('commitLoadFilter',

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