<%@ page contentType="text/html; charset=utf-8" %>

<html>
<head>
    <title>lothar</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>

<style type="text/css">

    .bg {
        display: none;
        position: fixed;
        width: 100%;
        height: 100%;
        background: #000;
        z-index: 2;
        top: 0;
        left: 0;
        opacity: 0.7;
        text-align: center;
        line-height:300px;
        font-size: large;
        color: white;
    }


    #container {
        min-width: 310px;
        max-width: 800px;
        height: 460px;
        margin: 0 auto
    }

    table.gridtable {
        width: 96%;
        font-family: verdana, arial, sans-serif;
        font-size: 11px;
        color: #333333;
        border-width: 1px;
        border-color: #666666;
        border-collapse: collapse;
    }

    table.gridtable th {
        border-width: 1px;
        padding: 8px;
        border-style: solid;
        border-color: #666666;
        background-color: #dedede;
    }

    table.gridtable td {
        border-width: 1px;
        padding: 8px;
        border-style: solid;
        border-color: #666666;
        background-color: #ffffff;
    }
</style>

<script type="text/javascript" src="lothars/highcharts/highcharts.js"></script>
<script type="text/javascript" src="lothars/jquery-3.2.1.min.js"></script>

<script type="text/javascript">

    var pageinfo = <%= request.getAttribute("pageinfo")%>;
    var data = <%= request.getAttribute("data")%>;
    var timeLine = <%= request.getAttribute("timeLine")%>;
    var pstr = "<%= request.getAttribute("pstr")%>";
    var alias = "<%= request.getAttribute("alias")%>";
    var stopStatus = <%= request.getAttribute("stop")%>;
    var methodName = pstr ? pstr.split('->')[0] : "";

    function goto(pi) {
        if (!pi) pi = {};
        if (!pi.page) pi.page = "";
        if (!pi.size) pi.size = "";
        if (!pi.last) pi.last = "";
        if (!pi.pstr) pi.pstr = "";
        if (!pi.match) pi.match = "";

        window.location.href = "/lothar?page=" + pi.page + "&size=" + pi.size + "&last=" + pi.last + "&pstr=" + pi.pstr + "&match=" + pi.match;
    }

    function goHome() {
        pageinfo.page = 1;
        pageinfo.match = "";
        pageinfo.pstr = "";
        goto(pageinfo);
    }

    function previousPage() {
        pageinfo.page = pageinfo.page - 1;
        goto(pageinfo);
    }

    function levelUp() {
        pageinfo.page = 1;
        pageinfo.match = "";
        pageinfo.pstr = pstr ? pstr.substr(0, pstr.lastIndexOf('->')) : "";
        goto(pageinfo);
    }

    function nextPage() {
        pageinfo.page = parseInt(pageinfo.page) + 1;
        goto(pageinfo);
    }

    function search() {
        pageinfo.page = 1;
        pageinfo.match = $("#ipt_match").val();
        goto(pageinfo);
    }

    function gotoMethod(method) {
        pageinfo.page = 1;
        pageinfo.pstr = method;
        pageinfo.match = "";
        goto(pageinfo);
    }

    function gotoSelect() {
        pageinfo.page = 1;
        pageinfo.pstr = "";
        var methods = [];
        $("input[name='selected']:checked").each(function (i,o) {
            methods.push(o.value);
        })
        if (methods.length == 0) return;
        pageinfo.match = methods.join("|");
        goto(pageinfo);
    }

    function submitPolicy() {
        var polices = [];
        var valid = true;
        $("#tb_policy").find("tr").each(function () {
            var po = {};
            $(this).find("input").each(function () {
                if (valid && this.name == 'threshold' && (!this.value || isNaN(this.value) || parseFloat(this.value)<=0)){
                    alert("阈值必须是大于0的数字");
                    valid = false;
                    return;
                }
                if (this.value) po[this.name] = this.value
            });
            polices.push(po);
        });

        if (!valid) return;

        $('.bg').fadeIn(200);
        $.post("/lothar/policy/add",
            {m: methodName, d: JSON.stringify(polices)},
            function (rep) {
                location.reload();
            }
        );
    }

    function addRow(row) {
        if (!row) {
            row = {};
            row.method = methodName;
        }

        row.desc = row.desc ? row.desc : "";
        row.join = row.join ? row.join : "";
        row.threshold = row.threshold ? row.threshold : "";
        row.conditions = row.conditions ? row.conditions : "";


        var newRow = '<tr>' +
            '<td><input type="text" disabled name="method" value="' + row.method + '"/></td>' +
            '<td><input type="text" name="threshold" value="' + row.threshold + '"/></td>' +
            '<td><input type="text" name="join" value="' + row.join + '"/></td>' +
            '<td><input type="text" name="conditions" value="' + row.conditions + '"/></td>' +
            '<td><input type="text" name="desc" value="' + row.desc + '"/></td>' +
            '<td><button onclick="delRow(this)">删除</button></td>' +
            '</tr>';

        $("#tb_policy").append(newRow);
        $("#tb_policy").find("input").css("width","98%");
    }

    function delRow(btn) {
        $(btn).parent().parent().remove();
    }

    function stop() {
        $('.bg').fadeIn(200);
        $.post("/lothar/stop/all",
            {s: !stopStatus},
            function (rep) {
                location.reload();
            }
        );
    }

    function addTableAll(row) {

        var newRow = '<tr>' +
            '<td><input type="checkbox" name="selected" value="'+row.name+'"/></td>' +
            '<td><a onclick="gotoMethod(\''+row.name+'\')" href="javascript:void(0)">' + row.name + '</a></td>' +
            '<td>' + row.data[row.data.length-1] + '</td>' +
            '<td>' + row.data[row.data.length-2] + '</td>' +
            '<td>' + row.data[row.data.length-3] + '</td>' +
            '</tr>';

        $("#tb_all").append(newRow);
    }


    $(document).ready(function () {

        var btns = $("#div_tab").find("button").hide();
        $("#lab_stop").css("color", stopStatus ? "red" : "green").text(stopStatus ? "停止" : "监控中...");

        if (pageinfo.match) $("#ipt_match").val(pageinfo.match);

        $.ajax({
            url: "/lothar/policy/get",
            dataType: "json",
            data: {m: methodName},
            success: function (data) {
                if (data){
                    $.each(data, function (i, o) {
                        addRow(o);
                    });
                }

                // 在单个方法页面的时候才可保存
                methodName ? $("#div_tab").find("button").show() : $("#div_tab").find("button").hide();
            },
            error: function () {
                alert("限流策略获取失败");
            }
        });


        $.ajax({
            url: "/lothar/all/get",
            dataType: "json",
            success: function (data) {
                if (data){
                    $.each(data, function (i, o) {
                        addTableAll(o);
                    });
                }
            },
            error: function () {
                alert("方法列表获取失败");
            }
        });

        var chart = Highcharts.chart('container', {


            title: {
                text: pstr ? pstr : "方法调用监控"
            },
            subtitle: {
                text: alias
            },
            xAxis: {
                categories: timeLine
            },
            yAxis: {
                title: {
                    text: '调用次数'
                }
            },
            series: data,

            plotOptions: {
                series: {
                    cursor: 'pointer',
                    events: {
                        click: function (event) {
                            if (this.name) {
                                var ps = pstr ? pstr + "->" : "";
                                pageinfo.page = 1;
                                pageinfo.pstr = ps + this.name.replace(/\([\s\S]*\)/g, "");
                                pageinfo.match = "";
                                goto(pageinfo);
                            }
                        }
                    }
                }
            },
            legend: {
                layout: 'vertical',
                align: 'right',
                verticalAlign: 'middle'
            }
        });

        $(window).resize(function () {
            chart.setSize(document.body.clientWidth - 10, 600);
        });
    });

</script>

<body style="text-align: center">
<div style="width: 98%">
    <button onclick="stop()">一键启停</button>&nbsp;
    状态：<label id="lab_stop"></label>&nbsp;
    <input type="text" id="ipt_match" style="width: 40%"/>&nbsp;<button onclick="search()">搜索</button>&nbsp;
    <button onclick="previousPage()">上一页</button>
    <button onclick="nextPage()">下一页</button>
    <button onclick="goHome()">首页</button>
    <button onclick="javascript:location.reload()">刷新</button>
    <button onclick="levelUp()">上一级</button>
    <a href="http://git.jd.com/popware/lothar/wikis/home" target="_blank">使用手册</a>
</div>

<div id="container" style="min-width: 98%"></div>

<div id="div_tab" style="text-align: left;margin-left: 5%;margin-right: 5%">
    <button onclick="addRow()">添加</button>
    <button onclick="submitPolicy()">保存</button>
    <table id="table" class="gridtable">
        <thead>
        <tr>
            <th width="40%">方法</th>
            <th width="100px">阈值/分</th>
            <th width="100px">and/or</th>
            <th width="20%">限流条件</th>
            <th>描述</th>
            <th width="44px">操作</th>
        </tr>
        </thead>
        <tbody id="tb_policy">
        </tbody>
    </table>
</div>
<br/>
<div id="div_tab_all" style="margin-left: 5%;margin-right: 5%">
    <table id="table_all" class="gridtable" style="text-align: center">
        <thead>
        <tr>
            <th width="70px"><button onclick="gotoSelect()">查看选中</button></th>
            <th>方法</th>
            <th width="100px">近一分钟</th>
            <th width="100px">近两分钟</th>
            <th width="100px">近三分钟</th>
        </tr>
        </thead>
        <tbody id="tb_all">
        </tbody>
    </table>
</div>
<div class="bg">正在处理...</div>
</body>
</html>