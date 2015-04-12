(function () {
	var app = angular.module('myse', ['ngRoute']);

	app.config(
			function ($routeProvider, $locationProvider) {
				$routeProvider
						.when(
								"/search",
								{
									templateUrl: '/static/search.html',
									controller: 'SearchCtrl',
									controllerAs: 'search',
									reloadOnSearch: false
								}
						)
						.when(
								"/setup",
								{
									templateUrl: '/static/setup-list.html',
									controller: 'SetupListCtrl',
									controllerAs: 'setup'
								}
						)
						.when(
								"/setup/source/edit/:sourceId",
								{
									templateUrl: '/static/setup-edit.html',
									controller: 'SetupEditCtrl',
									controllerAs: 'setup'
								}
						)
						.when(
								"/setup/source/edit",
								{
									templateUrl: '/static/setup-edit.html',
									controller: 'SetupEditCtrl',
									controllerAs: 'setup'
								}
						)
						.otherwise(
								{
									redirectTo: '/search'
								}
						);
			}
	);

	app.controller('NavCtrl', [
		'$location',
		function ($location) {
			this.isSelected = function (page) {
				return $location.path().startsWith(page);
			};
			this.class = function (page) {
				return {
					active: this.isSelected(page)
				};
			};
		}
	]);
	app.controller('Version', ['$http', function ($http) {
			this.props = {};
			var ctrl = this;
			$http.get('/rest/version').success(function (data) {
				ctrl.props = data;
			});
		}]);

	app.controller('SearchCtrl', ['$http', '$location', function ($http, $location) {
			// CONTROLLER CODE
			var ctrl = this;
			this.response = {
				results: [],
				error: null
			};
			this.queryChanged = function () {
				$http.post('/rest/search', {'q': this.query}).success(
						function (data) {
							ctrl.response = data;
						}
				);
				if (this.query !== '' && this.query !== false) {
					$location.search('q', this.query);
				} else {
					$location.search('q', null);
				}
			};
			this.query = $location.search()['q'];
			if (this.query !== undefined) {
				this.queryChanged();
			}
		}
	]
			);

	app.controller(
			'SetupListCtrl',
			['$http', '$window',
				function ($http, $window) {
					var ctrl = this;
					this.sources = [];
					this.fetchSources = function () {
						$http.get('/rest/setup/source/list').success(
								function (data) {
									ctrl.sources = data;
								});
					};

					this.delete = function (sourceId) {
						$http.get('/rest/setup/source/delete?id=' + sourceId).success(
								function (data) {
									if (data) {
										ctrl.fetchSources();
									}
								}
						);
					};

					this.deleteAfterConfirm = function (sourceId) {
						if ($window.confirm('Do you really want to delete this source ?')) {
							ctrl.delete(sourceId);
						}
					};

					this.fetchSources();
				}
			]);

	app.controller(
			'SetupEditCtrl',
			['$http', '$routeParams', '$location',
				function ($http, $routeParams, $location) {
					var ctrl = this;
					this.sourceId = $routeParams.sourceId;
					this.props = {};
					this.descs = {};
					this.types = [];

					if (this.sourceId !== undefined) {
						$http.get('/rest/setup/source/get?id=' + this.sourceId).success(
								function (data) {
									ctrl.props = data;

									ctrl.typeChanged();
								}
						);
					}

					$http.get('/rest/setup/source/types').success(
							function (data) {
								ctrl.types = data;
							}
					);

					this.typeChanged = function () {
						$http.get('/rest/setup/source/desc?type=' + this.props._type).success(
								function (data) {
									ctrl.descs = data;

									// If this is a new source,
									// we should set some default values
									if (ctrl.sourceId === undefined) {
										for (i = 0; i < ctrl.descs.length; ++i) {
											var d = ctrl.descs[i];
											if ( d.defaultValue !== undefined ) {
												ctrl.props[ d.name ] = d.defaultValue;
											}
										}
									}
								}
						);
					};

					this.save = function () {
						$http.post('/rest/setup/source/edit', this.props).success(
								function (data) {
									if (data) {
										$location.path('/setup');
									}
								}
						);
					};
				}
			]);

	app.directive(
			'myseLogs',
			function () {
				return {
					restrict: 'E',
					templateUrl: '/static/logs.html',
					controllerAs: 'logs',
					controller: ['$scope', '$http', '$window', function ($scope, $http, $window) {
							var ctrl = this;
							ctrl.maxRows = 0;
							ctrl.rows = [];
							ctrl.initWs = function () {
								if (ctrl.maxRows === 0) {
									return;
								}
								ctrl.ws = new WebSocket("ws://localhost:8080/ws");

								ctrl.ws.onmessage = function (evt) {
									obj = JSON.parse(evt.data);
									ctrl.rows.push(obj);
									ctrl.cleanup();
									$scope.$apply();
								};

								ctrl.cleanup = function () {
									while (ctrl.rows.length > ctrl.maxRows) {
										ctrl.rows.shift();
									}
								};

								ctrl.ws.onopen = function () {
									console.log("Opened!");
									ctrl.ws.send(JSON.stringify({type: "start"}));
								};

								ctrl.ws.onerror = function (err) {
									console.log("Error: " + err);
								};

								ctrl.ws.onclose = function () {
									console.log("Closed!");
								};
							};
							ctrl.initWs();

							ctrl.maxRowsChanged = function () {
								ctrl.rows = [];
								if (ctrl.ws !== undefined) {
									ctrl.ws.close();
								}
								ctrl.initWs();
							};

							ctrl.quit = function () {
								$http.get('/rest/quit').success(function () {
									$('body').html('');
								});
							};
						}]
				};
			}
	);

})();

$('#core').show();

// Source http://stackoverflow.com/a/646643/847202
if (typeof String.prototype.startsWith !== 'function') {
	// see below for better implementation!
	String.prototype.startsWith = function (str) {
		return this.indexOf(str) === 0;
	};
}