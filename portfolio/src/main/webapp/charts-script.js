google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawPieChart);

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