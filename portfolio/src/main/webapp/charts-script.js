google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);

/** Creates a chart and adds it to the page. */
function drawChart() {
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
      document.getElementById('chart-container'));
  chart.draw(data, options);
}