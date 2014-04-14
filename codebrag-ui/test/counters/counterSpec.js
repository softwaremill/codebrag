describe('Counter', function() {

    var Counter, counter;

    var AVAILABLE = true;
    var NOT_AVAILABLE = false;

    var initialCount = 10;
    var incomingCount = 20;
    var lowerCount = 5;

    beforeEach(module('codebrag.counters'));
    beforeEach(inject(function(_Counter_) {
        Counter = _Counter_;
    }));

    it('should have 0 count and no updates available on start', function() {
        counter = new Counter(initialCount);
        expect(counter.currentCount()).toBe(initialCount);
        expect(counter.updateAvailable()).toBe(NOT_AVAILABLE);
    });

    it('should tell when updates are available', function() {
        counter = new Counter(initialCount);
        counter.setIncomingTo(incomingCount);
        expect(counter.currentCount()).toBe(initialCount);
        expect(counter.updateAvailable()).toBe(AVAILABLE);
    });

    it('should replace counter and have no updates available', function() {
        counter = new Counter(initialCount);
        counter.setIncomingTo(incomingCount);
        counter.replace();
        expect(counter.currentCount()).toBe(incomingCount);
        expect(counter.updateAvailable()).toBe(NOT_AVAILABLE);
    });

    it('should decrease counter and replace counter', function() {
        counter = new Counter(initialCount);
        counter.setIncomingTo(incomingCount);
        counter.decrease();
        expect(counter.currentCount()).toBe(incomingCount - 1);
        expect(counter.updateAvailable()).toBe(NOT_AVAILABLE);
    });

    it('should have updates available when incoming count is lower than current', function() {
        counter = new Counter(initialCount);
        counter.setIncomingTo(lowerCount);
        expect(counter.currentCount()).toBe(initialCount);
        expect(counter.updateAvailable()).toBe(AVAILABLE);
    });
});