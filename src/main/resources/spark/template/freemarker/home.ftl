<!DOCTYPE html>
<html>

<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>

    <link rel="stylesheet" href="home/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="/css/main.css">
    <link rel="shortcut icon" href="data:image/x-icon;," type="image/x-icon">

    <link rel="stylesheet"
          href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,700,400italic">
    <link rel="stylesheet" href="home/css/user.css">
    <link href="https://fonts.googleapis.com/css?family=Poppins:300,400,500|Raleway:400,900" rel="stylesheet">

</head>
 <!--    Sidebar -->
    <div class="side-bar">
        <h3 id="business-name"></h3>
        <ul class="menu">
            <a href="https://twitter.com/intent/tweet?text=Be%20sure%20to%20stop%20by%20tomorrow%20and%20cool%20down%20with%20some%20%23coldbrew"><li class="menu-item" id="tweet">
                <div id="tweet-image">  </div>
            </li></a>
            <li class="menu-item" id="settings" data-toggle="modal" data-target="#myModal"></li>
            <li class="menu-item" id="logout"></li>
        </ul>
    </div>
    <div class="page-wrapper">
        <div class="menu-bar" font-size:23px;">
            <h1 class="page-heading" id="dashboard">Dashboard </h1>
            <ul class='toggles'>
                <input data-toggle="datepicker" id="start" placeholder="Start" style="display:none">
                <input data-toggle="datepicker" id="end" placeholder="End" style="display:none">
                <li class="underline" id="prev" style="display:none"<a href="#">back</a></li>
            </ul>
        </div>


        <div class="chart" id = "butt">
            <div id ="load">
                <div class="loader"></div>
            </div>

            <div class="row" id="overview-row">
                <div class="col-md-12">
                    <h2>Weekly Snapshot</h2>
                    <h4>Your next <span class="highlight">week</span> by the numbers </h4>
                    <div id="overview">
                        <div class="col-md-2 overview-item" id="predicted-total-sales">
                             <div class="col-md-4 shaded">
                                <div class="icon" id="predicted-total-sales-icon"></div>
                            </div>
                            <div class="col-md-8 overview-container">
                                <h3 class="overview-label" id="predicted-total-sales-value"></h3>
                                <h3 class="overview-label textual" >total sales</h3>
                            </div>
                        </div>
                        <div class="col-md-2 overview-item" id="predicted-visitors">
                            <div class="col-md-4 shaded">
                                <div class="icon" id="predicted-visitors-icon"></div>
                            </div>
                            <div class="col-md-8 overview-container">
                                <h3 class="overview-label" id="predicted-visitors-value"></h3>
                                <h3 class="overview-label textual" >visitors per day</h3>
                            </div>
                        </div>
                        <div class="col-md-2 overview-item" id="delta-sales">
                             <div class="col-md-4 shaded">
                                <div class="icon" id="delta-sales-icon"></div>
                            </div>
                            <div class="col-md-8 overview-container">
                                <h3 class="overview-label" id="delta-sales-value"></h3>
                                <h3 class="overview-label textual" >change since last week</h3>
                            </div>
                        </div>
                        <div class="col-md-2 overview-item" id="busiest-day">
                             <div class="col-md-4 shaded">
                                <div class="icon" id="busiest-day-icon"></div>
                            </div>
                            <div class="col-md-8 overview-container">
                                <h3 class="overview-label textual left" id="busiest-day-value"></h3>
                            </div>
                        </div>
                        <div class="col-md-2 overview-item" id="top-item">
                             <div class="col-md-4 shaded">
                                <div class="icon" id="top-item-icon"></div>
                            </div>
                            <div class="col-md-8 overview-container">
                                <h3 class="overview-label textual left" id="top-item-value"></h3>
                            </div>
                        </div>
                        <div class="col-md-2 overview-item" id="trending-item">
                             <div class="col-md-4 shaded">
                                <div class="icon" id="trending-item-icon"></div>
                            </div>
                            <div class="col-md-8 overview-container">
                                <h3 class="overview-label textual left" id="trending-item-value"></h3>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row" id="traffic-row">
                <div class="col-md-12">
                    <h2 id="traffic-trends">Past Trends</h2>
                    <h4>visitor <span class="highlight">traffic</span> data</h4>
                    <ul id="traffic-menu">
                        <li class="underline" id="by-week"><a>past week</a></li>
                        <li id="by-month"><a>past month</a></li>
                    </ul>

                    <div id="traffic"></div>

                    <div id="facts-container">
                        <ul class="facts-list">
                            <li class="fact" id="best">
                                <div class="wrapper">
                                    <div class="icon" id="best-day-icon"></div>
                                    <h4 class="fact-label">best day</h4>
                                    <span class="fact-value" id="best-day-value"></span>
                                </div>
                            </li>
                            <li class="fact" id="worst">
                                <div class="wrapper">
                                    <div class="icon" id="worst-day-icon"></div>
                                    <h4 class="fact-label">worst day</h4>
                                    <span class="fact-value" id="worst-day-value"></span>
                                </div>
                            </li>
                            <li class="fact" id="avg">
                                <div class="wrapper">
                                    <div class="icon" id="average-day-icon"></div><h4>average visitors per day</h4>
                                    <span class="fact-value" id="num-visitors">0</span>
                                </div>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>

            <div class="row" id="sales-row">
                <div class="col-md-12">
                    <h2>Sales</h2>
                    <h4>past <span class="highlight">data</span> and future <span class="highlight">predictions</span></h4>
                    <div id="container2"></div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <h2>By Product</h2>
                    <h4>item <span class="highlight">breakdowns</span></h4>

                    <div class="tech-stack-container" id="bubble-container">
                        <svg id="teck-stack-svg" width="100%" height="600" text-anchor="middle"></svg>
                    </div>
                </div>
            </div>

        </div>
    </div>


<!-- Modal -->
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
        <h2>Settings</h2>
        <div class="modal-body">
           <div class="dropdown">
                <label data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    <p id = "labelMenu">Locations
                        <span class="caret"></span>
                    </p>
                </label>
                <ul class="dropdown-menu" aria-labelledby="dLabel" id="list-locations">
                </ul>
            </div>
        </div>
        <button type="button" id = "loc" class="btn btn-primary">Change Location</button>
    </div>
  </div>
</div>
    <script src="home/js/jquery.min.js"></script>

    <script src="home/bootstrap/js/bootstrap.min.js"></script>

    <script src="https://code.highcharts.com/highcharts.js"></script>
    <script src="https://code.highcharts.com/modules/exporting.js"></script>

    <script src="/js/date_format.js"></script>
    <script src="/js/animateNumber/jquery.animateNumber.js"></script>
    <script src="/js/animateNumber/jquery.animateNumber.min.js"></script>
    <script src="home/js/queries.js"></script>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.18.1/moment.min.js"></script>
    <script src="home/js/Bubbles.js"></script>

    <link  href="/js/datepicker/dist/datepicker.css" rel="stylesheet">
    <script src="/js/datepicker/dist/datepicker.js"></script>
    <script src="home/js/packery.min.js"></script>

    <script src="home/js/cache.js"></script>
    <script type="text/javascript" async src="https://platform.twitter.com/widgets.js"></script>
    <script src="home/js/Home.js"></script>

    <script src="http://phuonghuynh.github.io/js/bower_components/d3/d3.min.js"></script>

    <script src="home/js/traffic.js"></script>
    <script src="home/js/itemGraph.js"></script>

    <script src="home/js/graph.js"></script>

    <script src="/js/charts/d3.min.js"></script>
    <script src="/js/charts/d3-legend.min.js"></script>
    <script src="home/js/bubbles_dynamic.js"></script>

</body>

</html>
