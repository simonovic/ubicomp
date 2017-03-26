var express = require('express'),
	app = express()
	db = require('./database')
	bodyParser = require('body-parser');

app.use(bodyParser.json());  

app.get('/sensorData', function(req, res) {
	console.log('getSensorData()\n');
	db.getSensorData(res);
});

app.post('/sensorData', function (req, res) {  
	console.log('insertSensorData()\n');
	console.log(req.body);
    db.insertSensorData(req.body.data, res); 
}); 

app.post('/filterSensorData', function(req, res) {
	console.log('filterSensorData()\n');
	console.log(req.body);
	db.filterSensorData(req.body.data, res);
});

app.get('/lastSensorData', function(req, res) {
	console.log('lastSensorData()\n');
	db.getLastSensorData(res);
});

app.get('/maxSensorDataByDay', function(req, res) {
	console.log('maxSensorDataByDay()\n');
	db.getMaxSensorDataByDay(req.query.timestamp, res);
});

app.listen(5000, function () {
  console.log('Uicomp-server listening on port 5000!');
});