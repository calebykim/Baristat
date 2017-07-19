$(document).ready(() => {
});

/* Gets total sales per unit time */
function getTotalSalesByUnit(start, end, unit, callback) {
	const postParameters = {
		start: start,
		end: end,
		unit: unit
	};

	$.post("/sales", postParameters, responseJSON => {
		const responseObject = JSON.parse(responseJSON);

		if (!responseObject.error) {
			callback(responseObject, null);
		} else {
			callback(null, responseObject.error);
		}
	});
}

/* Gets trend for trends section */
function getTrendsByTimeSpan(timeSpan, callback) {
	const postParameters = {
		timeSpan : timeSpan
	};

	$.post("/trends", postParameters, responseJSON => {
		const responseObject = JSON.parse(responseJSON);

		if (!responseObject.error) {
			callback(responseObject, null);
		} else {
			callback(null, responseObject.error);
		}
	});
}

/* Gets next week blurbs */
function getNextWeekOverview(callback) {
	$.post("/overview", {}, responseJSON => {
		const responseObject = JSON.parse(responseJSON);

		if (!responseObject.error) {
			callback(responseObject, null);
		} else {
			callback(null, responseObject.error);
		}
	});
}

/* Gets temperature per unit time */
function getTemperatureByUnit(start, end, unit, callback) {
	const postParameters = {
		start: start,
		end: end,
		unit: unit
	};

	$.post("/temperature", postParameters, responseJSON => {
		const responseObject = JSON.parse(responseJSON);

		if (!responseObject.error) {
			callback(responseObject, null);
		} else {
			callback(null, responseObject.error);
		}
	});
}

/* Gets precipitation per unit time */
function getPrecipitationByUnit(start, end, unit, callback) {
	const postParameters = {
		start: start,
		end: end,
		unit: unit
	};

	$.post("/precipitation", postParameters, responseJSON => {
		const responseObject = JSON.parse(responseJSON);

		if (!responseObject.error) {
			callback(responseObject, null);
		} else {
			console.log(responseObject.error);
			callback(null, responseObject.error);
		}
	});
}

/* Gets foot traffic */
function getFootTraffic(start, end, unit, callback) {
	const postParameters = {
		start: start,
		end: end,
		unit: unit
	};

	$.post("/traffic", postParameters, responseJSON => {
		const responseObject = JSON.parse(responseJSON);

		if (!responseObject.error) {
			callback(responseObject, null);
		} else {
			console.log(responseObject.error);
			callback(null, responseObject.error);
		}
	});
}

/* Gets number of employees */
function getNumEmployees(start, end, unit, callback) {
	const postParameters = {
		start: start,
		end: end,
		unit: unit
	};

	$.post("/employees", postParameters, responseJSON => {
		const responseObject = JSON.parse(responseJSON);

		if (!responseObject.error) {
			console.log(responseObject);
			callback(responseObject, null);
		} else {
			console.log(responseObject.error);
			callback(null, responseObject.error);
		}
	});
}

/* Gets total sales per unit time */
function getItemsByUnit(start, end, unit, callback) {
	const postParameters = {
		start: start,
		end: end,
		unit: unit
	};

	$.post("/items", postParameters, responseJSON => {
		const responseObject = JSON.parse(responseJSON);
		if (!responseObject.error) {
			callback(responseObject, null);
		} else {
			console.log(responseObject.error);
			callback(null, responseObject.error);
		}
	});
}


function getMerchant(callback) {

	$.post("/merchant", {}, responseJSON => {
		const responseObject = JSON.parse(responseJSON);
		callback(responseObject);
	});
}
