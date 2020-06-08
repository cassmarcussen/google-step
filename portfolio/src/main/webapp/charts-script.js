google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawCharts);

/* drawCharts draws all of the charts to display. It is called in setOnLoadCallback. 
The reasoning behind drawCharts is that setOnLoadCallback executes one function on load,
 and drawCharts provides a centralized place for which to load all of the charts on the website.
*/
function drawCharts(){
    //drawPieChart();
    drawGlobalWarmingChart();
}

/** Creates a chart and adds it to the page. */
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

  const options = {
    'title': 'My Pets',
    'width':500,
    'height':400
  };

  const chart = new google.visualization.PieChart(
      document.getElementById('pie-chart-container'));
  chart.draw(data, options);
}

/** Fetches bigfoot sightings data and uses it to create a chart. */
function drawGlobalWarmingChart() {
  fetch('/global-warming-sentiment-data').then(response => response.json())
  .then((globalWarmingSentiment) => {
    const data = new google.visualization.DataTable();
    //have Sentiment and frequency/amount of occurrence
    data.addColumn('string', 'Tweet');
    data.addColumn('number', 'Sentiment');
    Object.keys(globalWarmingSentiment).forEach((tweet) => {
      data.addRow([tweet, globalWarmingSentiment[tweet]]);
    });

    const options = {
      'title': 'Global Warming Sentiment',
      'width':600,
      'height':500,
       histogram: { bucketSize: 1 }
    };

    const chart = new google.visualization.Histogram(
        document.getElementById('globalwarming-chart-container'));
    chart.draw(data, options);
  });
}