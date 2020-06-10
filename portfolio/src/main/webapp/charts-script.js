google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawCharts);

/* drawCharts draws all of the charts to display. It is called in setOnLoadCallback. 
The reasoning behind drawCharts is that setOnLoadCallback executes one function on load,
 and drawCharts provides a centralized place for which to load all of the charts on the website.
*/
function drawCharts() {
    // Since we draw charts from different pages, we only want to add in particular charts if they are defined on that page.
    var pieChart = document.getElementById('pie-chart-container');
    if (typeof(pieChart) != 'undefined' && pieChart != null) {
        drawPieChart();
    }

    var gwChart = document.getElementById('globalwarming-chart-container');
    if (typeof(gwChart) != 'undefined' && gwChart != null) {
        drawGlobalWarmingChart();
    }

}

/** Creates a pie chart and adds it to the page. This pie chart represents the distribution of my pets. 
Since my pets are very important to me, I thought this would be a fun and interactive addition to the "Fun Facts" page. */
function drawPieChart() {
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'Pet Species');
  data.addColumn('number', 'Count');
        data.addRows([
          ['Cats', 2],
          ['Dogs', 2],
          ['Reptiles', 4],
          ['Birds', 3]
        ]);

  const pieChartOptions = {
    'title': 'My Pets',
    'width':500,
    'height':400
  };

  const chart = new google.visualization.PieChart(
      document.getElementById('pie-chart-container'));

  chart.draw(data, pieChartOptions);

}

/** Fetches global warming data and uses it to create a chart. */
function drawGlobalWarmingChart() {
  fetch('/global-warming-sentiment-data').then(response => response.json())
  .then((globalWarmingSentiment) => {
    const data = new google.visualization.DataTable();
    
    /*Sentiment is Yes or No, i.e. if the tweet affirms the existence of Global Warming or denies it.
    Number of Tweets is the number of tweets corresponding to each sentiment (Yes or No) */
    data.addColumn('string', 'Sentiment');
    data.addColumn('number', 'Number of Tweets');

    Object.keys(globalWarmingSentiment).forEach((sentiment) => {
      data.addRow([sentiment, globalWarmingSentiment[sentiment]]);
    });

    const gwChartOptions = {
      'title': 'Number of Tweets confirming (Yes) or denying (No) Global Warming',
      'width':500,
      'height':500,
      legend: 'none',
      bar: {groupWidth: '95%'},
      vAxis: { 
          gridlines: { count: 4 },
          'title': 'Number of Tweets'
       },
       hAxis: { 
          'title': 'Sentiment (Yes = confirm global warming, No = deny global warming)'
       }
    };

    const chart = new google.visualization.ColumnChart(
        document.getElementById('globalwarming-chart-container'));
    
    chart.draw(data, gwChartOptions);

  });
}