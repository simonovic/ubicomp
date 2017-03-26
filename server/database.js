var mysql = require('mysql');

var connection = mysql.createConnection({
	host: 		'localhost',
	user: 		'root',
	password: 	'simon',
	database: 	'ubicomp'
});

connection.connect(function(error) {
	if (!error) {
		console.log("Connected to database...");
	} else {
		console.log("Error connecting to database... ");
	}
}); 

module.exports = { 
	getSensorData: function(res) {
		connection.query('SELECT * FROM sensor_data', function(error, rows) {
			if (!error) {
				res.status(200).json({"data": rows});
			} else {
				res.status(500).json({ error: 'Error reading data from database!' });
			}
		});
	},

	insertSensorData: function(data, res) { 
		var dataArray = [];
		for (i=0; i<data.length; i++) { 
			dataArray.push([data[i].temperature, data[i].humidity, data[i].heat_index, data[i].air_quality, data[i].lat, data[i].lng, data[i].timestamp]); 
		}
		connection.query('INSERT INTO sensor_data (temperature, humidity, heat_index, air_quality, lat, lng, timestamp) VALUES ?', [dataArray], function(error, response) {
			if(!error) {
				res.status(200).json({ message: 'Data inserted in database!' });
			} else {
				res.status(500).json({ error: 'Error inserting data in database!' });
			}
		}); 
	},

	filterSensorData: function(data, res) { 
		var filterQuery = 'SELECT * FROM sensor_data WHERE ';
		var j = 0;
		if (data[0].type === 'radius') {
			j = 1;
			filterQuery += '6371 * '+ 
			'acos('+
	            'cos(radians('+data[0].lat+')) * '+
	            'cos(radians(lat)) *'+
	            'cos(radians(lng) - radians('+data[0].lng+')) + ' +
	            'sin(radians('+data[0].lat+')) * '+
	            'sin(radians(lat))) < '+data[0].distance;
		}
		if (data.length > 1) {
			filterQuery += " AND ";
		}
		for (i=j; i<data.length; i++) {
			var filter = data[i];
			filterQuery += filter.type+' BETWEEN '+connection.escape(filter.from)+' AND '+connection.escape(filter.to);
			if (i < data.length-1) 
				filterQuery += ' AND '
		}
		connection.query(filterQuery, function(error, rows) {
			if (!error) {
				res.status(200).json({"data": rows});
			} else {
				res.status(500).json({ error: 'Error filtering data!' });
			}
		});  
	},

	getLastSensorData: function(res) {
		connection.query('SELECT * FROM sensor_data ORDER BY id DESC LIMIT 1', function(error, row) {
			if (!error) {
				res.status(200).json({"data": row});
			} else {
				res.status(500).json({ error: 'Error geting last data from database!' });
			}
		});
	},

	getMaxSensorDataByDay: function(timestamp, res) {
		console.log(timestamp);
		connection.query('SELECT a.id, a.temperature, a.humidity, a.air_quality FROM sensor_data a INNER JOIN ' + 
			'(SELECT DATE(FROM_UNIXTIME(timestamp)) AS day, MAX(temperature) AS max from sensor_data ' + 
			'WHERE DATE(FROM_UNIXTIME(timestamp)) = DATE(FROM_UNIXTIME(' + connection.escape(timestamp) + ')) GROUP BY day) b ' + 
			'ON a.temperature = b.max AND DATE(FROM_UNIXTIME(timestamp)) = DATE(FROM_UNIXTIME(' + connection.escape(timestamp) + '))', 
			function(error, row) {
			if (!error) {
				res.status(200).json({"data": row});
			} else {
				res.status(500).json({ error: 'Error geting last data from database!' });
			}
		});
	}
};
