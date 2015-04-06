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
									controllerAs: 'search'
								}
						)
						.when(
								"/setup",
								{
									templateUrl: '/static/setup.html',
									controller: 'SetupCtrl',
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

	app.controller('NavCtrl', ['$route', '$routeParams', '$location',
		function ($route, $routeParams, $location) {
			this.$route = $route;
			this.$location = $location;
			this.$routeParams = $routeParams;
			this.current = 'search';
			ctrl = this;
			this.select = function (page) {
				this.current = page;
			};
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

	app.controller('SearchCtrl', ['$http', function ($http) {
			// CONTROLLER CODE
			var ctrl = this;
			this.query = '';
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
			};
		}
	]
			);

	app.controller('SetupCtrl', ['$http', function ($http) {
			var ctrl = this;
			this.sources = [];
			this.fetchSources = function () {
				$http.get('/rest/setup/source/list').success(
						function (data) {
							ctrl.sources = data;
						});
			};

			this.fetchSources();
		}]
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