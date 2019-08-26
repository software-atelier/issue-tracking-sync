var sync = {
    init: function () {
        sync._loadDefinedTrackingApplications();
        setInterval(sync.updateStatistics, 10000);
    },
    updateStatistics: function () {
        $.get('/statistics')
            .done(function (response) {
                var container = $('.sync-statistics');
                container.empty();
                Object.keys(response).forEach(function (key) {
                    var line = $("<div />");
                    line.text(key + ": " + response[key]);
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
                $('#manual-sync-status').text(data.message);
            },
            error: function (data) {
                $('#manual-sync-status').text("An error occurred: " + data.message);
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
