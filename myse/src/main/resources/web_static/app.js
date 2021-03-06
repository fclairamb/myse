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
								"/setup/user/list",
								{
									templateUrl: '/static/setup-user-list.html',
									controller: 'SetupUserListCtrl',
									controllerAs: 'setup'
								}
						)
						.when(
								"/setup/user/edit/:userId",
								{
									templateUrl: '/static/setup-user-edit.html',
									controller: 'SetupUserEditCtrl',
									controllerAs: 'setup'
								}
						)
						.when(
								"/setup/user/edit",
								{
									templateUrl: '/static/setup-user-edit.html',
									controller: 'SetupUserEditCtrl',
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
						.when(
								"/stats",
								{
									templateUrl: '/static/stats.html',
									controller: 'StatsCtrl',
									controllerAs: 'stats'
								}
						)
						.when(
								"/login",
								{
									templateUrl: '/static/login.html',
									controller: 'LoginCtrl',
									controllerAs: 'login'
								}
						)
						.when(
								"/logout",
								{
									templateUrl: '/static/logout.html',
									controller: 'LogoutCtrl',
									controllerAs: 'logout'
								}
						)
						.when(
								"/link/:docId",
								{
									templateUrl: '/static/link.html',
									controller: 'LinkCtrl',
									controllerAs: 'link'
								}
						)
						.when(
								"/first",
								{
									templateUrl: '/static/first.html',
									controller: 'FirstCtrl',
									controllerAs: 'first'
								}
						)
						.otherwise(
								{
									redirectTo: '/search',
									//redirectTo: '/first'
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
						return size + ' B';

					size /= 1024;

					if (size < 1024)
						return size.toFixed(1) + ' KB';

					size /= 1024;

					if (size < 1024)
						return size.toFixed(1) + ' MB';

					size /= 1024;

					if (size < 1024)
						return size.toFixed(1) + ' GB';

					size /= 1024;

					return size.toFixed(1) + ' TB';
				};
			});

	app.controller('NavCtrl', [
		'$location', '$http',
		function ($location, $http) {
			ctrl = this;
			app.nav = this;

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

			this.getAuthStatus = function() {
				$http.post('/rest/login').success(function (data) {
					app.nav.auth = data;
				});
			};

			this.getAuthStatus();
		}
	]);

	app.controller('Version', ['$http', function ($http) {
			this.props = {};
			var ctrl = this;
			$http.get('/rest/version').success(function (data) {
				ctrl.props = data;
			});
		}]);

	app.controller('LoginCtrl', [
		'$location', '$http',
		function ($location, $http) {
			ctrl = this;
			this.login = function () {
				$http.post('/rest/login', {'name': ctrl.name, 'pass': ctrl.pass}).success(function (data) {
					ctrl.response = data;
					app.nav.auth = data;
					if (ctrl.response.ok) {
						$location.path('/search');
					}
				});
			};

			this.focus = function() {
				delete ctrl.response;
			};
		}
	]);

	app.controller('LogoutCtrl', [
		'$location', '$http',
		function ($location, $http) {
			$http.get('/rest/logout').success(function () {
				app.nav.getAuthStatus();
			});
		}
	]);

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
									for (i = 0; i < ctrl.descs.length; ++i) {
										var d = ctrl.descs[i];
										if (d.defaultValue !== undefined && ctrl.props[ d.name ] === undefined) {
											ctrl.props[ d.name ] = d.defaultValue;
										}
									}
								}
						);
					};

					this.save = function () {
						$http.post('/rest/setup/source/edit?action=save', this.props).success(
								function (data) {
									if (data.nextUrl !== undefined) {
										window.location.href = data.nextUrl;
									} else if (data.ok) {
										$location.path('/setup/source/list');
									} else {
										window.alert('Something went WRONG ! ' + JSON.stringify(data));
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

					this.test = function () {
						delete ctrl.testResult;
						$http.post('/rest/setup/source/edit?action=test', this.props).success(
								function (data) {
									ctrl.testResult = data;
								}
						);
					};
				}
			]);

	app.controller(
			'SetupUserListCtrl',
			['$http', '$window',
				function ($http, $window) {
					var ctrl = this;
					this.users = [];
					this.fetchUsers = function () {
						$http.get('/rest/setup/user/list').success(
								function (data) {
									ctrl.users = data.users;
								});
					};

					this.delete = function (sourceId) {
						$http.get('/rest/setup/user/delete?id=' + sourceId).success(
								function (data) {
									if (data) {
										ctrl.fetchUsers();
									}
								}
						);
					};

					this.deleteAfterConfirm = function (sourceId) {
						if ($window.confirm('Do you really want to delete this user ?')) {
							ctrl.delete(sourceId);
						}
					};

					this.fetchUsers();
				}
			]);

	app.controller(
			'SetupUserEditCtrl',
			['$http', '$routeParams', '$location',
				function ($http, $routeParams, $location) {
					var ctrl = this;
					this.props = {'admin': false};

					if ($routeParams.userId !== undefined) {
						this.userId = $routeParams.userId;
					}

					if (this.userId !== undefined) {
						$http.get('/rest/setup/user/get?id=' + this.userId).success(
								function (data) {
									ctrl.props = data;
								}
						);
					}

					this.save = function () {
						$http.post('/rest/setup/user/edit', this.props).success(
								function () {
									$location.path('/setup/user/list');
								}
						);
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
							if (p.name === name && p.value !== undefined) {
								return p.value;
							}
						}
						return "";
					};

					this.edit = function (name) {
						this.values[ name ] = this.getValue(name);
					};

					this.editing = function (name) {
						return this.values[ name ] !== undefined;
					};

					this.save = function (name) {
						$http.get('/rest/setup/config?name=' + name + '&value=' + this.values[ name ]).success(
								function () {
									delete ctrl.values[name];
									ctrl.fetch();
								}
						);
					};

					this.cancel = function (name) {
						delete ctrl.values[name];
					};

					this.fetch();
				}
			]);

	app.controller('StatsCtrl',
			['$http', '$scope', '$timeout',
				function ($http, $scope, $timeout) {
					var ctrl = this;
					this.fetchPeriod = 3000; // 3s
					this.data = {};

					this.fetch = function () {
						$http.get('/rest/stats').success(
								function (data) {
									ctrl.data = data;
								});
					};

					this.regularFetch = function () {
						this.timeoutPromise = $timeout(ctrl.regularFetch, ctrl.fetchPeriod);
						ctrl.fetch();
					};

					$scope.$on('$destroy', function () {
						$timeout.cancel(this.timeoutPromise);
					});

					this.regularFetch();
				}
			]);

	app.controller('LinkCtrl',
			['$http', '$scope', '$routeParams',
				function ($http, $scope, $routeParams) {
					var ctrl = this;
					this.docId = $routeParams.docId;
					this.data = {'name': '(loading)'};

					$http.get('/rest/link?docId=' + ctrl.docId).success(
							function (data) {
								ctrl.data = data;
								ctrl.considerLink();
							});

					ctrl.considerLink = function () {
						if (ctrl.data.type === 'DIRECT') {
							window.location.href = ctrl.data.address;
						}
					};
				}
			]);

	app.controller('FirstCtrl',
			['$http',
				function ($http) {
					app.nav.checkAuth();
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