var sync = {
    init: function () {
        setInterval(sync.updateStatistics, 10000);
    },
    updateStatistics: function () {
        $.get('/statistics')
            .done(function (response) {
                alert("success");
                $("#mypar").html(response.amount);
            });
    }
};

$(document).ready(function () {
    sync.init();
});
