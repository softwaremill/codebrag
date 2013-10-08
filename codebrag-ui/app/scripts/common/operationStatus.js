var codebrag = codebrag || {};

codebrag.OperationStatus = function() {

    var self = this;

    this.setErr = function() {
        _set('err');
    };

    this.setOk = function() {
        _set('ok');
    };

    this.setPending = function() {
        _set('pending');
    };

    this.clear = function() {
        this.ok = null;
        this.err = null;
        this.pending = null;
    };

    function _set(prop, msg) {
        self.clear();
        self[prop] = msg || true;
    }

};
