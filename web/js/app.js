// Draws a line chart on a plain canvas. No library is loaded from the internet,
// so the page still works on a machine with no network.
// The LAST value is the session being viewed, it is marked with a red dot.
function drawSeries(canvasId, values, labelText) {
    var cv = document.getElementById(canvasId);
    if (!cv || !values || values.length === 0)
        return;
    var ctx = cv.getContext('2d');
    var w = cv.width, h = cv.height, pad = 34;

    var min = values[0], max = values[0];
    for (var i = 1; i < values.length; i++) {
        if (values[i] < min)
            min = values[i];
        if (values[i] > max)
            max = values[i];
    }
    if (max === min) {
        max = min + 1;
    }

    ctx.clearRect(0, 0, w, h);
    ctx.strokeStyle = '#999';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(pad, pad / 2);
    ctx.lineTo(pad, h - pad);
    ctx.lineTo(w - 8, h - pad);
    ctx.stroke();

    ctx.strokeStyle = '#1a6fb5';
    ctx.lineWidth = 2;
    ctx.beginPath();
    var lastX = 0, lastY = 0;
    for (var j = 0; j < values.length; j++) {
        var x = pad + (w - pad - 12) * j / (values.length - 1 || 1);
        var y = (h - pad) - (h - pad - pad / 2) * (values[j] - min) / (max - min);
        if (j === 0)
            ctx.moveTo(x, y);
        else
            ctx.lineTo(x, y);
        lastX = x;
        lastY = y;
    }
    ctx.stroke();

    ctx.fillStyle = '#d33';
    ctx.beginPath();
    ctx.arc(lastX, lastY, 4, 0, Math.PI * 2);
    ctx.fill();

    ctx.fillStyle = '#333';
    ctx.font = '11px sans-serif';
    ctx.fillText(max.toFixed(2), 2, pad / 2 + 4);
    ctx.fillText(min.toFixed(2), 2, h - pad);
    ctx.fillText(labelText, pad + 4, 12);
}