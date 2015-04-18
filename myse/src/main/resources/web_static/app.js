(function () {
	var app = angular.module('myse', ['ngRoute', 'filters']);

	app.config(
			function ($routeProvider) {
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
								"/setup/source/list",
								{
									templateUrl: '/static/setup-source-list.html',
									controller: 'SetupSourceListCtrl',
									controllerAs: 'setup'
								}
						)
						.when(
								"/setup/source/edit/:sourceId",
								{
									templateUrl: '/static/setup-source-edit.html',
									controller: 'SetupSourceEditCtrl',
									controllerAs: 'setup'
								}
						)
						.when(
								"/setup/source/edit/copyFrom/:copySourceId",
								{
									templateUrl: '/static/setup-source-edit.html',
									controller: 'SetupSourceEditCtrl',
									controllerAs: 'setup'
								}
						)
						.when(
								"/setup/source/edit",
								{
									templateUrl: '/static/setup-source-edit.html',
									controller: 'SetupSourceEditCtrl',
									controllerAs: 'setup'
								}
						)
						.when(
								"/setup/config",
								{
									templateUrl: '/static/setup-config-list.html',
									controller: 'SetupConfigListCtrl',
									controllerAs: 'config'
								}
						)
						.otherwise(
								{
									redirectTo: '/search'
								}
						);
			}
	);

	// Source: https://gist.github.com/yrezgui/5653591
	angular.module('filters', [])
			.filter('filesize', function () {
				return function (size) {
					if (isNaN(size))
						size = 0;

					if (size < 1024)
						return size + ' Bytes';

					size /= 1024;

					if (size < 1024)
						return size.toFixed(2) + ' Kb';

					size /= 1024;

					if (size < 1024)
						return size.toFixed(2) + ' Mb';

					size /= 1024;

					if (size < 1024)
						return size.toFixed(2) + ' Gb';

					size /= 1024;

					return size.toFixed(2) + ' Tb';
				};
			});

	app.controller('NavCtrl', [
		'$location', '$http',
		function ($location, $http) {
			this.isSelected = function (page) {
				return $location.path().startsWith(page);
			};
			this.class = function (page) {
				return {
					active: this.isSelected(page)
				};
			};

			this.quit = function () {
				if (confirm('Really quit the app ?')) {
					$http.get('/rest/quit').success(function () {
						$('body').html('');
						window.close()
					});
				}
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

	app.controller('SearchCtrl', ['$http', '$location', '$scope', '$sce', function ($http, $location, $scope, $sce) {
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

			$scope.trusted_html = function (html_code) {
				return $sce.trustAsHtml(html_code);
			};
		}
	]
			);

	app.controller(
			'SetupSourceListCtrl',
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
			'SetupSourceEditCtrl',
			['$http', '$routeParams', '$location',
				function ($http, $routeParams, $location) {
					var ctrl = this;
					this.props = {};
					this.descs = {};
					this.types = [];

					if ($routeParams.sourceId !== undefined) {
						this.sourceId = $routeParams.sourceId;
					} else if ($routeParams.copySourceId !== undefined) {
						this.sourceId = $routeParams.copySourceId;
						this.copy = true;
					}

					if (this.sourceId !== undefined) {
						$http.get('/rest/setup/source/get?id=' + this.sourceId).success(
								function (data) {
									ctrl.props = data;

									if (ctrl.copy) {
										delete ctrl.props['_id'];
									}

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
											if (d.defaultValue !== undefined) {
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
										$location.path('/setup/source/list');
									}
								}
						);
					};

					this.setToDefault = function (name) {
						for (i = 0; i < ctrl.descs.length; ++i) {
							var d = ctrl.descs[i];
							if (d.name === name) {
								ctrl.props[ name ] = d.defaultValue;
							}
						}
					};
				}
			]);

	app.controller('SetupConfigListCtrl',
			['$http', '$routeParams', '$location',
				function ($http, $routeParams, $location) {
					var ctrl = this;
					this.editedParameters = {};
					this.values = {};
					this.parameters = [];

					this.fetch = function () {
						$http.get('/rest/setup/config/list').success(
								function (data) {
									ctrl.parameters = data;
								});
					};

					this.getValue = function (name) {
						for (i = 0; i < ctrl.parameters.length; ++i) {
							var p = ctrl.parameters[i];
							if (p.name === name) {
								return p.value;
							}
						}
					};

					this.edit = function (name) {
//						console.log("edit " + name);
						this.values[ name ] = this.getValue(name);
					};

					this.editing = function (name) {
//						console.log("editing " + name + " : " + this.values[ name ]);
						return this.values[ name ] !== undefined;
					};

					this.save = function (name) {
//						console.log("save " + name);
						$http.get('/rest/setup/config?name=' + name + '&value=' + this.values[ name ]).success(
								function (data) {
									ctrl.values[ name ] = undefined;

									ctrl.fetch();
								}
						);
					};

					this.fetch();
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
								ctrl.ws = new WebSocket("ws://" + window.location.host + "/ws");

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