var sync = {
    init: function () {
        sync._loadDefinedTrackingApplications();
        sync.updateStatistics();
        setInterval(sync.updateStatistics, 10000);
    },
    updateStatistics: function () {
        $.get('/statistics')
            .done(function (response) {
                var container = $('.sync-statistics');
                container.empty();
                Object.keys(response).forEach(function (key) {
                    var line = $("<div />");
                    var value = key.indexOf("EnqueueTime") >= 0 ? response[key] + " ms" : response[key];
                    line.text(key + ": " + value);
                    container.append(line);
                });
            });
    },

    runManual: function () {
        var frm = $('form#manual-sync-form');
        var requestBody = {
            "trackingsystem": $("[name=trackingsystem]", frm).val(),
            "issuekey": $("[name=issuekey]", frm).val()
        }
        $.ajax({
            url: '/manualsync',
            type: 'PUT',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify(requestBody),
            success: function (data) {
                $('#manual-sync-error').text("").hide();
                $('#manual-sync-status').text(data.message).show();
            },
            error: function (data) {
                $('#manual-sync-error').text("An error occurred: " + data.message).show();
                $('#manual-sync-status').text("").hide();
            }
        });
    },

    _loadDefinedTrackingApplications: function () {
        $.get('/definedSystems')
            .done(function (response) {
                var selectControl = $('select#trackingsystem');
                selectControl.empty();
                if (response && response.length > 0) {
                    response.forEach(function (appName) {
                        var opt = $("<option value='" + appName + "'>" + appName + "</option>");
                        selectControl.append(opt);
                    });
                }
            });
    }
};

$(document).ready(function () {
    sync.init();
});
