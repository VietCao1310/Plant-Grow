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


// Scatter chart on a plain canvas, no library. Each point is semi-transparent,
// so places where many points overlap look darker.
function drawScatter(canvasId, xs, ys, xLabel, yLabel) {
    var cv = document.getElementById(canvasId);
    if (!cv || !xs || xs.length === 0)
        return;
    var ctx = cv.getContext('2d');
    var w = cv.width, h = cv.height;
    var padL = 46, padB = 36, padT = 18, padR = 12;

    var minX = xs[0], maxX = xs[0], minY = ys[0], maxY = ys[0];
    for (var i = 1; i < xs.length; i++) {
        if (xs[i] < minX) minX = xs[i];
        if (xs[i] > maxX) maxX = xs[i];
        if (ys[i] < minY) minY = ys[i];
        if (ys[i] > maxY) maxY = ys[i];
    }
    if (maxX === minX) maxX = minX + 1;
    if (maxY === minY) maxY = minY + 1;

    ctx.clearRect(0, 0, w, h);
    ctx.strokeStyle = '#999';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(padL, padT);
    ctx.lineTo(padL, h - padB);
    ctx.lineTo(w - padR, h - padB);
    ctx.stroke();

    ctx.fillStyle = 'rgba(26, 111, 181, 0.45)';
    for (var j = 0; j < xs.length; j++) {
        var x = padL + (w - padL - padR) * (xs[j] - minX) / (maxX - minX);
        var y = (h - padB) - (h - padB - padT) * (ys[j] - minY) / (maxY - minY);
        ctx.beginPath();
        ctx.arc(x, y, 3, 0, Math.PI * 2);
        ctx.fill();
    }

    ctx.fillStyle = '#333';
    ctx.font = '11px sans-serif';
    ctx.fillText(maxY.toFixed(1), 4, padT + 4);
    ctx.fillText(minY.toFixed(1), 4, h - padB);
    ctx.fillText(minX.toFixed(0), padL, h - padB + 14);
    ctx.fillText(maxX.toFixed(0), w - padR - 30, h - padB + 14);
    ctx.fillText(xLabel, padL + (w - padL - padR) / 2 - 50, h - 6);
    ctx.fillText(yLabel, padL + 6, 12);
}


// Pot chart: several lines on ONE time axis, plus a dashed vertical line for every watering.
// Each line has its own vertical scale (weight in grams, moisture and light in other units),
// so the legend lists the min and max of every line. No library is used.
// series: [{name, color, points: [[timeMs, value], ...]}, ...]   marks: [timeMs, ...]
function drawPotChart(canvasId, legendId, series, marks) {
    var cv = document.getElementById(canvasId);
    if (!cv)
        return;
    var ctx = cv.getContext('2d');
    var w = cv.width, h = cv.height;
    var padL = 16, padR = 16, padT = 16, padB = 44;

    var tMin = Infinity, tMax = -Infinity, i, j;
    for (i = 0; i < series.length; i++) {
        for (j = 0; j < series[i].points.length; j++) {
            var t = series[i].points[j][0];
            if (t < tMin) tMin = t;
            if (t > tMax) tMax = t;
        }
    }
    for (i = 0; i < marks.length; i++) {
        if (marks[i] < tMin) tMin = marks[i];
        if (marks[i] > tMax) tMax = marks[i];
    }
    if (!isFinite(tMin))
        return;
    if (tMax === tMin)
        tMax = tMin + 1;

    function xOf(t) {
        return padL + (w - padL - padR) * (t - tMin) / (tMax - tMin);
    }
    function p2(n) {
        return n < 10 ? '0' + n : '' + n;
    }
    function fmt(t) {
        var d = new Date(t);
        return p2(d.getDate()) + '/' + p2(d.getMonth() + 1) + ' ' + p2(d.getHours()) + ':' + p2(d.getMinutes());
    }

    ctx.clearRect(0, 0, w, h);

    // horizontal guide lines
    ctx.strokeStyle = '#e3e3e3';
    ctx.lineWidth = 1;
    for (i = 0; i <= 4; i++) {
        var gy = padT + (h - padB - padT) * i / 4;
        ctx.beginPath();
        ctx.moveTo(padL, gy);
        ctx.lineTo(w - padR, gy);
        ctx.stroke();
    }

    // one dashed vertical line per watering
    ctx.strokeStyle = 'rgba(120, 120, 120, 0.35)';
    ctx.setLineDash([3, 3]);
    for (i = 0; i < marks.length; i++) {
        var mx = xOf(marks[i]);
        ctx.beginPath();
        ctx.moveTo(mx, padT);
        ctx.lineTo(mx, h - padB);
        ctx.stroke();
    }
    ctx.setLineDash([]);

    // time axis with 6 labels
    ctx.strokeStyle = '#999';
    ctx.beginPath();
    ctx.moveTo(padL, h - padB);
    ctx.lineTo(w - padR, h - padB);
    ctx.stroke();
    ctx.fillStyle = '#333';
    ctx.font = '11px sans-serif';
    for (i = 0; i <= 5; i++) {
        var tt = tMin + (tMax - tMin) * i / 5;
        var label = fmt(tt);
        var lx = xOf(tt);
        var tw = ctx.measureText(label).width;
        ctx.fillText(label, Math.max(2, Math.min(w - tw - 2, lx - tw / 2)), h - padB + 16);
    }

    // the lines, each scaled between its own min and max
    var legend = '';
    for (i = 0; i < series.length; i++) {
        var pts = series[i].points;
        if (pts.length === 0)
            continue;
        var vMin = pts[0][1], vMax = pts[0][1];
        for (j = 1; j < pts.length; j++) {
            if (pts[j][1] < vMin) vMin = pts[j][1];
            if (pts[j][1] > vMax) vMax = pts[j][1];
        }
        var span = (vMax === vMin) ? 1 : (vMax - vMin);
        ctx.strokeStyle = series[i].color;
        ctx.lineWidth = 2;
        ctx.beginPath();
        for (j = 0; j < pts.length; j++) {
            var px = xOf(pts[j][0]);
            var py = (h - padB) - (h - padB - padT) * (pts[j][1] - vMin) / span;
            if (j === 0) ctx.moveTo(px, py);
            else ctx.lineTo(px, py);
        }
        ctx.stroke();
        legend += '<span style="display:inline-block;margin-right:18px;">'
                + '<span style="display:inline-block;width:14px;height:4px;background:' + series[i].color
                + ';vertical-align:middle;margin-right:5px;"></span>'
                + series[i].name + ': ' + vMin.toFixed(1) + ' - ' + vMax.toFixed(1) + '</span>';
    }
    var lg = document.getElementById(legendId);
    if (lg)
        lg.innerHTML = legend;
}