describe('Local flash-like messages', function() {

    var Flash;

    beforeEach(module('codebrag.common.services'));

    beforeEach(inject(function (_Flash_) {
        Flash = _Flash_;
    }));

    it('should add all messages from array to flash', function() {
        // given
        var f = new Flash();
        var messages = ['foo', 'bar'];

        // when
        f.addAll('error', messages);

        // then
        expect(getMessagesOfType(f, 'error')).toEqual(messages);
    });

    it('should add all messages from messages map to flash', function() {
        // given
        var f = new Flash();
        var messages = {
            foo: ['far', 'faz'],
            boo: ['bar', 'baz']
        };

        // when
        f.addAll('error', messages);

        // then
        var expectedMessages = ['far', 'faz', 'bar', 'baz'];
        expect(getMessagesOfType(f, 'error')).toEqual(expectedMessages);
    });

    function getMessagesOfType(flash, type) {
        return flash.get(type).map(function(m) {
            return m.message;
        });
    }

});