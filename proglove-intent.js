var exec = require('cordova/exec');

var ProGloveIntent = {
    /**
     * Registers a persistent listener for ProGlove button gesture events.
     * successCallback is invoked every time a DISPLAY_BUTTON intent arrives,
     * with an object: { buttonId: string, gesture: "DOUBLE_CLICK" | "TRIPLE_CLICK" }
     */
    registerListener: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'ProGloveIntentPlugin', 'register', []);
    }
};

module.exports = ProGloveIntent;
